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
	private static String version = "V2.09";
	private static boolean verbose = false;
	private static int ticksPerDot, ticks;

	enum TestCase {
		CREATE(0, 0.73), //
		WRITE_SMALL(200, 0.48), //
		READ_SMALL(200, 0.14), //
		WRITE_BIG(50000000, 550.7), //
		READ_BIG(50000000, 57.92), //
		ITERATE(0, 0.12), //
		READMOD(0, 0.13), //
		DELETE(0, 2.36), //
		JGIT_ADD_MODIFIED(0, 6.02), //
		;
		public int size;
		public double standard;

		TestCase(int size, double standard) {
			this.standard = standard;
			this.size = size;
		}
	}

	public static void main(String args[]) throws IOException, GitAPIException {
		long start, elapsedTime;
		byte buffer[];
		long chksum = 0;
		double sum = 0;
		String tmpDirName = System.getProperty("java.io.tmpdir");

		// handle parameters
		for (String arg : args)
			if (arg.equals("-h")) {
				System.err.println("JavaFileIOPerf [-h] [-v] [tmpDir]");
				System.exit(1);
			} else if (arg.equalsIgnoreCase("-v"))
				verbose = true;
			else
				tmpDirName = arg;
		File baseDir = new File(tmpDirName, "JGitTests_"
				+ System.currentTimeMillis());

		// print information about the test environment
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Properties sysProps = System.getProperties();
		// @formatter:off
		System.out.println("Java File I/O perf " 
				+ version
				+ ", date:" + df.format(new Date())
				+ ", hostName="	+ java.net.InetAddress.getLocalHost().getHostName()
				+ ", java.version=" + sysProps.getProperty("java.version")
				+ ", os.name=" + sysProps.getProperty("os.name")
				+ ", os.arch=" + sysProps.getProperty("os.arch")
				+ ", user.name=" + sysProps.getProperty("user.name")
				);
		// @formatter:on
		System.out.println("basedir=" + baseDir.getPath());
		System.out
				.println("score: performance compared to Windows7(64bit) Notebook with QuadCore I5, 8 GB Ram, standard NTFS disk, Virusscanner turned off.");
		System.out.println("       Higher scores mean better performance.");

		// fill a buffer with some test data
		int maxSize = 0;
		for (TestCase c : TestCase.values())
			maxSize = Math.max(maxSize, c.size);
		buffer = new byte[maxSize];
		verboseStart("fill a buffer with " + maxSize + " bytes",
				maxSize);
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) ((97 * i) % 23);
			verboseTick();
		}
		verboseStop();

		File[] subDirs = null;
		try {
			// prepare files/folders
			verboseStart("preparation: create %d empty directories", 8*8*8);
			subDirs = new File[8 * 8 * 8];
			for (int i = 0, level0 = 0; level0 < 8; level0++) {
				for (int level1 = 0; level1 < 8; level1++) {
					for (int level2 = 0; level2 < 8; level2++) {
						subDirs[i] = new File(baseDir, Integer.toString(level0)
								+ "/" + Integer.toString(level1) + "/"
								+ Integer.toString(level2));
						subDirs[i++].mkdirs();
						verboseTick();
					}
				}
			}
			verboseStop();

			int rounds;
			int newrounds;
			int preparedTo;
			int offset;

			// measure creating files
			newrounds = 3;
			offset = 0;
			preparedTo = 0;
			for (;;) {
				rounds = newrounds;
				verboseStart("measure creation of %d empty files", rounds);
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.createNewFile())
						throw new IOException("Fatal: failed to create file !"
								+ tf.getPath());
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				offset += rounds;
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.CREATE.standard * rounds / elapsedTime;
			describe("Creating new files", elapsedTime, rounds,
					TestCase.CREATE.standard, TestCase.CREATE.size);
			analyzeFSTimerTicks(subDirs, "creation", rounds, offset - rounds);

			// measure writing small files
			newrounds = 3;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: create %d empty files", offset + rounds - preparedTo);
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
					verboseTick();
				}
				verboseStop();

				verboseStart("measure writing "+TestCase.WRITE_SMALL.size+" bytes into each of to %d files", rounds);
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					FileOutputStream fos = new FileOutputStream(tf);
					fos.write(buffer, 0, TestCase.WRITE_SMALL.size);
					fos.close();
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.WRITE_SMALL.standard * rounds / elapsedTime;
			describe("Writing small files", elapsedTime, rounds,
					TestCase.WRITE_SMALL.standard, TestCase.WRITE_SMALL.size);
			analyzeFSTimerTicks(subDirs, "update", rounds, offset);

			// measure reading small files
			newrounds = 3;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: fill %d files with "+TestCase.WRITE_SMALL.size+" bytes", offset+512-preparedTo);
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
					verboseTick();
				}
				verboseStop();

				verboseStart("measure reading "+TestCase.WRITE_SMALL.size+" bytes from each of to %d files", rounds);
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
					if (j == offset + 512)
						j = offset;
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READ_SMALL.standard * rounds / elapsedTime;
			describe("Reading small files", elapsedTime, rounds,
					TestCase.READ_SMALL.standard, TestCase.READ_SMALL.size);

			// measure writing big files
			newrounds = 3;
			offset = 0;
			for (;;) {
				verboseStart("preparation: create %d files", offset+rounds-preparedTo);
				rounds = newrounds;
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
					verboseTick();
				}
				verboseStop();

				verboseStart("measure writing "+TestCase.WRITE_BIG.size+" bytes to each of to %d files", rounds);
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					FileOutputStream fos = new FileOutputStream(tf);
					fos.write(buffer, 0, TestCase.WRITE_BIG.size);
					fos.close();
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.WRITE_BIG.standard * rounds / elapsedTime;
			describe("Writing big files", elapsedTime, rounds,
					TestCase.WRITE_BIG.standard, TestCase.WRITE_BIG.size);

			// measure reading big files
			newrounds = 3;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: fill %d files with "+TestCase.WRITE_BIG.size+" bytes", offset+16-preparedTo);
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
					verboseTick();
				}
				verboseStop();

				verboseStart("measure reading "+TestCase.WRITE_BIG.size+" bytes from each of to %d files", rounds);
				start = System.currentTimeMillis();
				int j = offset;
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
					if (j == offset + 16)
						j = offset;
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READ_BIG.standard * rounds / elapsedTime;
			describe("Reading big files", elapsedTime, rounds,
					TestCase.READ_BIG.standard, TestCase.READ_BIG.size);

			// measure listing files
			newrounds = 3;
			offset = 0;
			preparedTo = rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: check that we have %d files", offset+rounds-preparedTo);
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
					verboseTick();
				}
				verboseStop();

				verboseStart("measure listing %d files", rounds);
				start = System.currentTimeMillis();
				listDir(baseDir, rounds);
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.ITERATE.standard * rounds / elapsedTime;
			describe("Listing files in a hierachy", elapsedTime, rounds,
					TestCase.ITERATE.standard, TestCase.ITERATE.size);

			// measure reading of lastmodified of files
			newrounds = 3;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: check that we have %d files", offset + rounds-preparedTo);
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
					verboseTick();
				}
				verboseStop();

				verboseStart("measure reading lastmodified from %d files", rounds);
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					chksum += tf.lastModified();
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.READMOD.standard * rounds / elapsedTime;
			describe("Reading modification times", elapsedTime, rounds,
					TestCase.READMOD.standard, TestCase.READMOD.size);

			// measure deleting files
			newrounds = 3;
			offset = 0;
			preparedTo = offset + rounds;
			for (;;) {
				rounds = newrounds;
				verboseStart("preparation: check that we have %d files", offset+rounds-preparedTo);
				for (int i = preparedTo; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					if (!tf.exists())
						if (!tf.createNewFile())
							throw new IOException(
									"Fatal: failed to create file "
											+ tf.getPath() + "!");
					preparedTo++;
					verboseTick();
				}
				verboseStop();

				verboseStart("measure deleting %d files", rounds);
				start = System.currentTimeMillis();
				for (int i = offset; i < offset + rounds; i++) {
					File tf = new File(subDirs[i & 0x01FF], Integer.toString(i));

					tf.delete();
				}
				elapsedTime = System.currentTimeMillis() - start;
				verboseStop();
				newrounds = calcNewRounds(rounds, elapsedTime);
				if (newrounds <= rounds)
					break;
			}
			sum += TestCase.DELETE.standard * rounds / elapsedTime;
			describe("Deleting files", elapsedTime, rounds,
					TestCase.DELETE.standard, TestCase.DELETE.size);

			// write summary

			System.out.printf("Average score: %.2f, chksum:%d\n", sum / 8.0,
					chksum);

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
			for (int r = 0; r < 500; r++) {
				fos = new FileOutputStream(w);
				fos.write(65 + r % 32);
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

			describe("modify file and add it with jgit", elapsedTime, 500,
					TestCase.JGIT_ADD_MODIFIED.standard,
					TestCase.JGIT_ADD_MODIFIED.size);
		} finally {
			System.out.println("Cleaning up: please be patient ...");
			delete(baseDir);
			System.out.println("Finished. Goodbye!");
		}
	}

	private static void verboseStart(String message, int maxSize) {
		if (verbose) {
			System.out.printf(message, Math.max(0,  maxSize));
			ticksPerDot = maxSize / 10;
			ticks = 0;
		}
	}

	private static void verboseTick() {
		if (verbose) {
			if (ticks >= ticksPerDot) {
				System.out.print(".");
				ticks = 0;
			} else
				ticks++;
		}
	}

	private static void verboseStop() {
		if (verbose)
			System.out.println(". Done!");
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
									/ (double) rounds, (double) standard
									* (double) rounds / ((double) elapsedTime));
		} else
			System.out
					.printf("%s: #files: %d, filesize: %d(bytes), overall time: %d(ms), time/file: %.2f(ms), throughput: %.2f(Mbyte/s), score: %.2f\n",
							message, rounds, size, elapsedTime,
							(double) elapsedTime / (double) rounds,
							((double) rounds * (double) size * 1000.0)
									/ ((double) elapsedTime * 1024.0 * 1024.0),
							(double) standard * (double) rounds
									/ ((double) elapsedTime));
	}
}
