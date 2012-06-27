/**
 *
 */
public class StopWatch {
	private long start = -1;
	private long readOut = 0;

	public static StopWatch createAndStart() {
		StopWatch sw = new StopWatch();
		sw.start();
		return sw;
	}

	/**
	 */
	public StopWatch start() {
		if (start == -1)
			start = System.currentTimeMillis();
		return this;
	}

	/**
	 * @return readout
	 */
	public StopWatch stop(String message) {
		if (start != -1) {
			readOut += System.currentTimeMillis() - start;
			start = -1;
		}
		if (message != null)
			System.out.println("Stop of " + message + ". readOut:" + readOut);
		return this;
	}

	/**
	 * @return readout
	 */
	public long readout(String message) {
		long ret = (start != -1) ? readOut + System.currentTimeMillis() - start
				: readOut;
		if (message != null)
			System.out.println("readOut(" + message + "):" + ret);
		return ret;
	}
}
