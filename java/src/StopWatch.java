/**
 * A minimal stopwatch
 */
public class StopWatch {
	private long start = -1;
	private long readOut = -1;

	public static StopWatch createAndStart() {
		StopWatch sw = new StopWatch();
		sw.start();
		return sw;
	}

	public long start() {
		start = System.currentTimeMillis();
		return start;
	}

	public long stop() {
		if (start == -1)
			return -1;
		long lastMeasurement = System.currentTimeMillis() - start;
		readOut += lastMeasurement;
		start = -1;
		return lastMeasurement;
	}

	public long readout() {
		return (start != -1) ? readOut + System.currentTimeMillis() - start
				: readOut;
	}
}
