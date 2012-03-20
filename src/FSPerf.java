import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

public class FSPerf {
	private static final int RUNS = 4;
	private static final int REPETIONSPERRUN = 6;
	private static final int FILES = 1000;
	private static final int MAXFILESIZE = 10000000;

	private static byte[][] testData = new byte[FILES][];
	private static File file[] = new File[FILES];

	static {
		for (int j = 0; j < FILES; j++) {
			testData[j] = new byte[j * j];
			for (int k = 0; k < (j * j) % MAXFILESIZE; k++)
				testData[j][k] = (byte) (k % 256);
		}
	}

	public static void main(String args[]) throws IOException, GitAPIException,
			InterruptedException {
		// create a temporary directory for our test repo
		File baseDir = new File(System.getProperty("java.io.tmpdir"),
				"JGitTests_" + System.currentTimeMillis());
		if (!baseDir.mkdir())
			throw new IOException("Couldn't create temporary directory "
					+ baseDir.getPath());

		System.out.println("Working in dir: " + baseDir.getPath());
		long timestamps_c[] = new long[(RUNS-1) * REPETIONSPERRUN];
		long timestamps_r[] = new long[(RUNS-1) * REPETIONSPERRUN];
		long timestamps_l[] = new long[(RUNS-1) * REPETIONSPERRUN];
		long timestamps_w[] = new long[(RUNS-1) * REPETIONSPERRUN];
		long timestamps_d[] = new long[(RUNS-1) * REPETIONSPERRUN];

		int tsCnt=0;
		long prev, act;
		for (int j = 0; j < RUNS; j++)
			for (int i = 0; i < REPETIONSPERRUN; i++) {
				File parent = new File(baseDir, "FSPerf_" + j + "_" + i);
				if (!parent.mkdirs())
					throw new IOException("fatal: Couldn't mkdirs() on "
							+ parent.getPath());
				prev = System.currentTimeMillis();
				createTestData(parent);
				act = System.currentTimeMillis();
				timestamps_c[tsCnt] = act - prev;
				prev = act;
				writeTestData(parent);
				act = System.currentTimeMillis();
				timestamps_w[tsCnt] = act - prev;
				prev = act;
				listTestData(parent);
				act = System.currentTimeMillis();
				timestamps_l[tsCnt] = act - prev;
				prev = act;
				readTestData(parent);
				act = System.currentTimeMillis();
				timestamps_r[tsCnt] = act - prev;
				prev = act;
				deleteTestData(parent);
				act = System.currentTimeMillis();
				timestamps_d[tsCnt] = act - prev;
				prev = act;
				if (j>0)
					tsCnt++;
				System.out.println(".");
			}

		timestamps_c = clean(timestamps_c);
		timestamps_w = clean(timestamps_w);
		timestamps_l = clean(timestamps_l);
		timestamps_r = clean(timestamps_r);
		timestamps_d = clean(timestamps_d);

		System.out.println("Executed each operation " + timestamps_c.length
				+ " times. data="+out(timestamps_r));
		System.out.printf(
				"CREATE: speed=%.2f avg=%.2f(ms), stdDev=%.2f(ms), min=%d, max=%d\n",
				avg(timestamps_c)/4.55, avg(timestamps_c), stdDev(timestamps_c), min(timestamps_c),
				max(timestamps_c));
		System.out.printf(
				"WRITE: speed=%.2f avg=%.2f(ms), stdDev=%.2f(ms), min=%d, max=%d\n",
				avg(timestamps_w)/52.08, avg(timestamps_w), stdDev(timestamps_w), min(timestamps_w),
				max(timestamps_w));
		System.out.printf(
				"LIST: speed=%.2f avg=%.2f(ms), stdDev=%.2f(ms), min=%d, max=%d\n",
				avg(timestamps_l)/0.01, avg(timestamps_l), stdDev(timestamps_l), min(timestamps_l),
				max(timestamps_l));
		System.out.printf(
				"READ: speed=%.2f avg=%.2f(ms), stdDev=%.2f(ms), min=%d, max=%d\n",
				avg(timestamps_r)/3.21, avg(timestamps_r), stdDev(timestamps_r), min(timestamps_r),
				max(timestamps_r));
		System.out.printf(
				"DELETE: speed=%.2f avg=%.2f(ms), stdDev=%.2f(ms), min=%d, max=%d\n",
				avg(timestamps_d)/8.92, avg(timestamps_d), stdDev(timestamps_d), min(timestamps_d),
				max(timestamps_d));
	}

	/**
	 * @param parent
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void createTestData(File parent)
			throws InterruptedException, IOException {
		for (int i = 0; i < FILES; i++) {
			file[i] = new File(parent, String.valueOf(i));
			file[i].createNewFile();
		}
	}

	/**
	 * @param parent
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void writeTestData(File parent) throws InterruptedException,
			IOException {
		for (int i = 0; i < FILES; i++) {
			FileOutputStream out = new FileOutputStream(file[i]);
			out.write(testData[i]);
			out.flush();
			out.close();
		}
	}

	/**
	 * @param parent
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void listTestData(File parent) throws InterruptedException,
			IOException {
		parent.listFiles();
	}

	/**
	 * @param parent
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void readTestData(File parent) throws InterruptedException,
			IOException {
		byte buff[] = new byte[MAXFILESIZE];
		for (int i = 0; i < FILES; i++) {
			// assert(file[i].exists());
			FileInputStream in = new FileInputStream(file[i]);
			int read = in.read(buff, 0, testData[i].length);
			// assert(read==testData[i].length);
			in.close();
		}
	}

	/**
	 * @param parent
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void deleteTestData(File parent)
			throws InterruptedException, IOException {
		for (int i = 0; i < FILES; i++) {
			// assert(file[i].exists());
			file[i].delete();
			file[i] = null;
			// assert(!file[i].exists());
		}
	}

	public static long[] clean(long[] values) {
		int minIdx = 0;
		int maxIdx = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > values[maxIdx])
				maxIdx = i;
			if (values[i] < values[minIdx])
				minIdx = i;
		}
		long[] ret = new long[values.length - 2];
		int j = 0;
		for (int i = 0; i < values.length; i++)
			if (i != minIdx && i != maxIdx)
				ret[j++] = values[i];
		return ret;
	}

	public static double avg(long[] values) {
		long sum = 0;
		for (long l : values)
			sum += l;
		return ((double) sum) / ((double) values.length);
	}

	public static double stdDev(long[] values) {
		double avg = avg(values);
		long sum = 0;
		for (long l : values)
			sum += Math.abs((double) l - avg);
		return (double) sum / (double) values.length;
	}

	public static long min(long[] values) {
		long min = Long.MAX_VALUE;
		for (long l : values)
			if (l < min)
				min = l;
		return min;
	}

	public static long max(long[] values) {
		long max = Long.MIN_VALUE;
		for (long l : values)
			if (l > max)
				max = l;
		return max;
	}

	public static String out(long[]values) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (long l : values) {
			if (!first)
				b.append(",");
			first = false;
			b.append(l);
		}
		return b.toString();
	}
}
