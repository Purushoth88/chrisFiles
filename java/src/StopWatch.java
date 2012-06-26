

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StopWatch {
	private long start=-1;
	private long readOut=0;
	private long lastLeap = 0;
	private String message;
	private boolean print;

	List<String> leapMessages=new ArrayList<String>(20);
	List<Long> leapTimes=new ArrayList<Long>(20);

	/**
	 * @param message
	 * @param print
	 */
	public StopWatch(String message, boolean print) {
		this.message = message;
		this.print = print;
	}

	/**
	 */
	public void start() {
		if (start != -1)
			return;
		readOut = 0;
		lastLeap = 0;
		start = System.currentTimeMillis();
		if (print)
			System.out.println("Start of " + message);
	}

	/**
	 * @return readout
	 */
	public long stop() {
		if (start != -1) {
			readOut = System.currentTimeMillis() - start;
			start = -1;
		}
		if (print)
			System.out.println("Stop of " + message + ". elapsedTime:"
					+ readOut);
		return readOut;
	}

    /**
     * @return readout
     */
    public long readout() {
		if (start != -1) {
			readOut = System.currentTimeMillis() - start;
		}
		return readOut;
    }

    /**
	 * @param leapMsg
	 */
    public void leap(String leapMsg) {
		long r = readout();
		if (print)
			System.out.println("Leap in " + message + ". " + leapMsg
					+ ": elapsedTime since start:" + r
					+ ": elapsedTime since last leap:" + (r - lastLeap));
		lastLeap = r;
		leapTimes.add(r);
    	leapMessages.add(leapMsg);
    }
}
