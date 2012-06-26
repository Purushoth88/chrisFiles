import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectInserter;

/**
 * Measures how fast Java can work on files. It creates, lists, reads (content
 * and metadata), writes, deletes files.
 */
public class JavaFileIOPerf {
	private static final String TEST_REPO = "testRepo";

	enum TestCase {
		CREATE(5000, 1, 0, 0.73), //
		WRITE_SMALL(5000, 1, 200, 0.48), //
		READ_SMALL(5000, 10, 200, 0.14), //
		WRITE_BIG(2, 1, 50000000, 550.7), //
		READ_BIG(2, 10, 50000000, 57.92), //
		ITERATE(5000, 10, 0, 0.12), //
		READMOD(5000, 10, 0, 0.13), //
		DELETE(5000, 1, 0, 2.36), //
		JGIT_ADD_MODIFIED(1, 500, 0, 6.02), //
		;
		public int nrOfFiles;
		public int size;
		public double standard;
		public int repetitions;

		TestCase(int nrOfFiles, int repetitions, int size, double standard) {
			this.nrOfFiles = nrOfFiles;
			this.standard = standard;
			this.size = size;
			this.repetitions = repetitions;
		}
	}

	private static String version = "V2.05";

	public static void main(String args[]) throws IOException, GitAPIException {
		long start, elapsedTime;
		byte buffer[];
		long chksum = 0;
		double sum = 0;

		// handle parameters
		if (args.length > 0 && "-h".equalsIgnoreCase(args[0])) {
			System.err.println("JavaFileIOPerf [-h] [tmpDir]");
			System.exit(1);
		}
		String tmpDirName = (args.length == 0) ? System
				.getProperty("java.io.tmpdir") : args[0];
		File baseDir = new File(tmpDirName, "JGitTests_"
				+ System.currentTimeMillis());

		// print information about the test environment
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Properties sysProps = System.getProperties();
		System.out.println("Java File I/O perf " + version + ", date:"
				+ df.format(new Date()) + ", hostName="
				+ java.net.InetAddress.getLocalHost().getHostName()
				+ ", java.version=" + sysProps.getProperty("java.version")
				+ ", os.name=" + sysProps.getProperty("os.name") + ", os.arch="
				+ sysProps.getProperty("os.arch"));
		System.out.println("basedir=" + baseDir.getPath());
		System.out
				.println("score: performance compared to Windows7(64bit) Notebook with QuadCore I5, 8 GB Ram, standard NFTS disk, Virusscanner turned off.");
		System.out
				.println("       Higher scores mean better performance.");

		// fill a buffer with some test data
		int maxSize = 0;
		for (TestCase c : TestCase.values())
			maxSize = Math.max(maxSize, c.size);
		buffer = new byte[maxSize];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) ((97 * i) % 23);

		File[] subDirs = null;
		try {
			// prepare files/folders
			subDirs = new File[8 * 8 * 8];
			for (int i = 0, level0 = 0; level0 < 8; level0++) {
				for (int level1 = 0; level1 < 8; level1++) {
					for (int level2 = 0; level2 < 8; level2++) {
						subDirs[i] = new File(baseDir, Integer.toString(level0)
								+ "/" + Integer.toString(level1) + "/"
								+ Integer.toString(level2));
						subDirs[i++].mkdirs();
					}
				}
			}

			int rounds;
			int newrounds;
			int preparedTo;
			int offset;

			// measure creating files
			newrounds = 1;
			offset = 0;
			preparedTo = 0;
			for (;;) {
				rounds = newrounds;
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.createNewFile())
						throw new IOException("Fatal: failed to create file !"
								+ tf.getPath());
				}
				elapsedTime = System.currentTimeMillis() - start;
				offset += rounds;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.CREATE.standard*rounds/elapsedTime;
			describe("Creating new files", elapsedTime, rounds,
					TestCase.CREATE.standard, TestCase.CREATE.size);
			analyzeFSTimerTicks(subDirs, "creation", rounds, offset-rounds);

			// measure writing small files
			newrounds = 1;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists()) 
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
				}

				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					FileOutputStream fos = new FileOutputStream(tf);
					fos.write(buffer, 0, TestCase.WRITE_SMALL.size);
					fos.close();
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.WRITE_SMALL.standard*rounds/elapsedTime;
			describe("Writing small files", elapsedTime, rounds,
					TestCase.WRITE_SMALL.standard, TestCase.WRITE_SMALL.size);
			analyzeFSTimerTicks(subDirs, "update", rounds, offset);

			// measure reading small files
			newrounds = 1;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + 512; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					if (tf.length() != TestCase.WRITE_SMALL.size) {
						FileOutputStream fos = new FileOutputStream(tf);
						fos.write(buffer, 0, TestCase.WRITE_SMALL.size);
						fos.close();
					}
					preparedTo++;
				}

				start = System.currentTimeMillis();
				int j = offset;
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[j & 0x01FF], Integer.toString(j));
					FileInputStream fis = new FileInputStream(tf);
					int read, total = 0;
					do {
						read = fis.read(buffer, total, TestCase.READ_SMALL.size
								- total);
						if (read == -1)
							throw new IOException("Fatal: Short read!");
						total += read;
					} while (total < TestCase.READ_SMALL.size);
					fis.close();
					
					j++;
					if (j==offset+512)
						j=offset;
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READ_SMALL.standard*rounds/elapsedTime;
			describe("Reading small files", elapsedTime, rounds,
					TestCase.READ_SMALL.standard, TestCase.READ_SMALL.size);

			// measure writing big files
			newrounds = 1;
			offset = 0;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
				}

				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					FileOutputStream fos = new FileOutputStream(tf);
					fos.write(buffer, 0, TestCase.WRITE_BIG.size);
					fos.close();
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.WRITE_BIG.standard*rounds/elapsedTime;
			describe("Writing big files", elapsedTime, rounds,
					TestCase.WRITE_BIG.standard, TestCase.WRITE_BIG.size);

			// measure reading big files
			newrounds = 1;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + 16; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					if (tf.length() != TestCase.WRITE_BIG.size) {
						FileOutputStream fos = new FileOutputStream(tf);
						fos.write(buffer, 0, TestCase.WRITE_BIG.size);
						fos.close();
					}
					preparedTo++;
				}

				start = System.currentTimeMillis();
				int j=offset;
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[j & 0x01FF], Integer.toString(j));

					FileInputStream fis = new FileInputStream(tf);
					int read, total = 0;
					do {
						read = fis.read(buffer, total, TestCase.READ_BIG.size
								- total);
						if (read == -1)
							throw new IOException("Fatal: Short read!");
						total += read;
					} while (total < TestCase.READ_BIG.size);
					fis.close();
					
					j++;
					if (j==offset+16)
						j=offset;
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READ_BIG.standard*rounds/elapsedTime;
			describe("Reading big files", elapsedTime, rounds,
					TestCase.READ_BIG.standard, TestCase.READ_BIG.size);

			// measure listing files
			newrounds = 1;
			offset = 0;
			preparedTo = rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
				}

				start = System.currentTimeMillis();
				listDir(baseDir, rounds);
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.ITERATE.standard*rounds/elapsedTime;
			describe("Listing files in a hierachy", elapsedTime, rounds,
					TestCase.ITERATE.standard, TestCase.ITERATE.size);

			// measure reading of lastmodified of files
			newrounds = 1;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
				}

				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					chksum += tf.lastModified();
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READMOD.standard*rounds/elapsedTime;
			describe("Reading modification times", elapsedTime, rounds,
					TestCase.READMOD.standard, TestCase.READMOD.size);

			// measure deleting files
			newrounds = 1;
			offset=0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
				}

				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					tf.delete();
				}
				elapsedTime = System.currentTimeMillis() - start;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.DELETE.standard*rounds/elapsedTime;
			describe("Deleting files", elapsedTime, rounds,
					TestCase.DELETE.standard, TestCase.DELETE.size);

			// write summary
			System.out
					.printf("Average score: %.2f(ms), chksum:%d\n",
							sum/8.0, chksum);

			System.out.println("Additional tests which don't go into sum:");
			// measure jgit add modified file
			Git git = Git.init().setDirectory(new File(baseDir, TEST_REPO))
					.setBare(false).call();
			File w = new File(git.getRepository().getWorkTree(), "a");
			w.createNewFile();
			FileOutputStream fos = new FileOutputStream(w);
			fos.write(65);
			fos.close();
			DirCache dc = git.getRepository().lockDirCache();
			DirCacheBuilder builder = dc.builder();
			DirCacheEntry dce = new DirCacheEntry("a");
			dce.setFileMode(FileMode.REGULAR_FILE);
			dce.setLength(1);
			builder.add(dce);
			builder.commit();
			ObjectInserter inserter = git.getRepository().newObjectInserter();

			start = System.currentTimeMillis();
			for (int r = 0; r < TestCase.JGIT_ADD_MODIFIED.repetitions; r++)
				for (int i = 0; i < TestCase.JGIT_ADD_MODIFIED.nrOfFiles; i++) {
					fos = new FileOutputStream(w);
					fos.write(65 + i % 32);
					fos.close();
					dc = git.getRepository().lockDirCache();
					builder = dc.builder();
					dce.setLastModified(w.lastModified());
					FileInputStream fis = new FileInputStream(w);
					dce.setObjectId(inserter.insert(Constants.OBJ_BLOB, 1, fis));
					fis.close();
					builder.add(dce);
					builder.commit();
				}
			elapsedTime = System.currentTimeMillis() - start;
			
			describe("modify file and add it with jgit", elapsedTime, TestCase.JGIT_ADD_MODIFIED.repetitions*TestCase.JGIT_ADD_MODIFIED.nrOfFiles, TestCase.JGIT_ADD_MODIFIED.standard, TestCase.JGIT_ADD_MODIFIED.size);
		} finally {
			System.out.println("Cleaning up: please be patient ...");
			delete(baseDir);
			System.out.println("Finished. Goodbye!");
		}
	}

	private static int calcNewRounds(int rounds, long elapsedTime) {
		if (elapsedTime < 500)
			rounds *= 10;
		else if (elapsedTime < 2500)
			rounds = (int) (rounds * 3000.0 / elapsedTime);
		return Math.min(200000, rounds);
	}

	private static void delete(File f) {
		if (f.isDirectory())
			for (File c : f.listFiles())
				delete(c);
		f.delete();
	}

	private static int listDir(File baseDir, int maxFiles) {
		int cnt = 0;
		for (File c : baseDir.listFiles()) {
			if (c.isDirectory())
				cnt += listDir(c, maxFiles - cnt);
			else
				cnt++;
			if (cnt >= maxFiles)
				break;
		}
		return cnt;
	}

	private static void analyzeFSTimerTicks(File[] sdir, String action,
			int nrOfFiles, int offset) {
		// Analyze files system timer behavior
		int ticks = 0;
		long cur, last = new File(sdir[offset & 0x01FF],
				Integer.toString(offset)).lastModified();
		int lastTick = 0, firstTick = 0;
		long minIncrease = Long.MAX_VALUE;
		for (int i = offset + 1; i < offset + nrOfFiles; i++) {
			cur = new File(sdir[i & 0x01FF], Integer.toString(i))
					.lastModified();
			if (cur != last) {
				minIncrease = Math.min(cur - last, minIncrease);
				ticks++;
				if (firstTick == 0)
					firstTick = i;
				lastTick = i;
				last = cur;
			}
		}
		System.out
				.printf("Detected filesystem timer increases during %s of %d files: %d. Minimal increase of the timer: %d(ms). Average number of files in one timer slot: %.2f\n",
						action, nrOfFiles, ticks, minIncrease,
						((double) lastTick - firstTick) / (ticks - 1));
	}

	private static void describe(String message, long elapsedTime, int rounds,
			double standard, int size) {
		if (size <= 0) {
			System.out
					.printf("%s: #files: %d, overall time: %d(ms), time/file: %.2f(ms), score: %.2f\n",
							message, rounds, elapsedTime, (double) elapsedTime
									/ rounds, (double) standard * rounds
									/ ((double) elapsedTime));
		} else
			System.out
					.printf("%s: #files: %d, filesize: %d(bytes), overall time: %d(ms), time/file: %.2f(ms), throughput: %.2f(Mbyte/s), score: %.2f\n",
							message, rounds, size, elapsedTime,
							(double) elapsedTime / rounds,
							(rounds * size * 1000.0)
									/ (elapsedTime * 1024.0 * 1024.0),
							(double) standard * rounds / ((double) elapsedTime));
	}
}
