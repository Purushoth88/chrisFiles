import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingTest {
	private static final Logger log = Logger.getLogger(LoggingTest.class
			.getName());

	public static void main(String[] args) {
		log.log(Level.CONFIG, "logging to CONFIG");
		log.log(Level.SEVERE, "logging to SEVERE");
		log.log(Level.WARNING, "logging to WARNING");
		log.log(Level.INFO, "logging to INFO");
		log.log(Level.FINE, "logging to FINE");
		log.log(Level.FINER, "logging to FINER");
		log.log(Level.FINEST, "logging to FINEST");
		log.log(Level.ALL, "logging to ALL");
		log.log(Level.OFF, "logging to OFF");

		LoggingTest loggingTest = new LoggingTest();
		loggingTest.f("in LoggingTest.f(). ");
		
		for (Handler h :log.getHandlers())
			System.err.println("Found handler: "+h);

		for (Handler h :Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getHandlers())
			System.err.println("found global handler: "+h);
	}

	private void f(String msg) {
		log.log(Level.CONFIG, msg + " logging to CONFIG");
		log.log(Level.SEVERE, msg + "logging to SEVERE");
		log.log(Level.WARNING, msg + "logging to WARNING");
		log.log(Level.INFO, msg + "logging to INFO");
		log.log(Level.FINE, msg + "logging to FINE");
		log.log(Level.FINER, msg + "logging to FINER");
		log.log(Level.FINEST, msg + "logging to FINEST");
		log.log(Level.ALL, msg + "logging to ALL");
		log.log(Level.OFF, msg + "logging to OFF");
	}
}
