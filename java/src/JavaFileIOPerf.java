import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * TODO document this type
 */
public class JavaFileIOPerf {
	private static final int NR_OF_BIG_FILES = 30;
	private static final int NR_OF_SMALL_FILES = 10000;
	private static final int BIG_FILE_SIZE = 50000000;
	private static final int SMALL_FILE_SIZE = 1000;

	static byte buffer[] = new byte[BIG_FILE_SIZE];

	public static void main(String args[]) throws IOException {
		if (args.length > 0 && "-h".equalsIgnoreCase(args[0])) {
			System.err.println("JavaFileIOPerf [-h] [tmpDir]");
			System.exit(1);
		}
		String tmpDirName = (args.length == 0) ? System
				.getProperty("java.io.tmpdir") : args[0];
		File baseDir = new File(tmpDirName, "JGitTests_"
				+ System.currentTimeMillis());
		if (!baseDir.mkdir())
			throw new IOException("Couldn't create temporary directory "
					+ baseDir.getPath());

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Properties sysProps = System.getProperties();
		System.out.println("Java File I/O perf ("+dateFormat.format(new Date())+"):"
				+ " java.version="+sysProps.getProperty("java.version")
				+ ", os.name="+sysProps.getProperty("os.name")
				+ ", os.arch="+sysProps.getProperty("os.arch")
				+ ", basedir=" + baseDir.getPath()
				);

		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) i;

		long writeSmall = measureFileIO(false, baseDir, "small",
				NR_OF_SMALL_FILES, SMALL_FILE_SIZE);
		long writeBig = measureFileIO(false, baseDir, "big", NR_OF_BIG_FILES,
				BIG_FILE_SIZE);
		long readSmall = measureFileIO(true, baseDir, "small",
				NR_OF_SMALL_FILES, SMALL_FILE_SIZE);
		long readBig = measureFileIO(true, baseDir, "big", NR_OF_BIG_FILES,
				BIG_FILE_SIZE);

		System.out
				.printf("Reading %d files of size %d(bytes): overall=%d(ms), time per file=%.2f(ms), throughput=%.2f(MByte/s)\n",
						NR_OF_SMALL_FILES, SMALL_FILE_SIZE, readSmall,
						(float) readSmall / NR_OF_SMALL_FILES,
						((float) SMALL_FILE_SIZE * NR_OF_SMALL_FILES * 1000)
								/ (1024.0 * 1024.0 * readSmall));
		System.out
				.printf("Writing %d files of size %d(bytes): overall=%d(ms), time per file=%.2f(ms), throughput=%.2f(MByte/s)\n",
						NR_OF_SMALL_FILES, SMALL_FILE_SIZE, writeSmall,
						(float) writeSmall / NR_OF_SMALL_FILES,
						((float) SMALL_FILE_SIZE * NR_OF_SMALL_FILES * 1000)
								/ (1024.0 * 1024.0 * writeSmall));
		System.out
				.printf("Reading %d files of size %d(bytes): overall=%d(ms), time per file=%.2f(ms), throughput=%.2f(MByte/s)\n",
						NR_OF_BIG_FILES, BIG_FILE_SIZE, readBig,
						(float) readBig / NR_OF_BIG_FILES,
						((float) BIG_FILE_SIZE * NR_OF_BIG_FILES * 1000)
								/ (1024.0 * 1024.0 * readBig));
		System.out
				.printf("Writing %d files of size %d(bytes): overall=%d(ms), time per file=%.2f(ms), throughput=%.2f(MByte/s)\n",
						NR_OF_BIG_FILES, BIG_FILE_SIZE, writeBig,
						(float) writeBig / NR_OF_BIG_FILES,
						((float) BIG_FILE_SIZE * NR_OF_BIG_FILES * 1000)
								/ (1024.0 * 1024.0 * writeBig));

		for (int i = 0; i < NR_OF_SMALL_FILES; i++)
			(new File(baseDir, "small" + i)).delete();
		for (int i = 0; i < NR_OF_BIG_FILES; i++)
			(new File(baseDir, "big" + i)).delete();
		baseDir.delete();
	}

	private static long measureFileIO(boolean read, File parentFolder,
			String fileNamePrefix, int repetitions, int size)
			throws IOException {
		long ioTime = 0, start;
		for (int i = 0; i < repetitions; i++) {
			File f = new File(parentFolder, fileNamePrefix + i);
			if (read) {
				FileInputStream fis = new FileInputStream(f);
				start = System.currentTimeMillis();
				if (fis.read(buffer, 0, size) != size)
					throw new IOException("Fatal: Short read!");
				fis.close();
				ioTime += System.currentTimeMillis() - start;
			} else {
				FileOutputStream fos = new FileOutputStream(f);
				start = System.currentTimeMillis();
				fos.write(buffer, 0, size);
				fos.close();
				ioTime += System.currentTimeMillis() - start;
			}
		}
		return ioTime;
	}
}
