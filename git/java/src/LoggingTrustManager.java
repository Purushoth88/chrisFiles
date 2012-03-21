import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoggingTrustManager implements X509TrustManager {
	private PrintStream output;
	
	public static void register(PrintStream output) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] {new LoggingTrustManager(output)}, new java.security.SecureRandom());
		HttpsURLConnection
				.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
	
	public LoggingTrustManager(PrintStream output) {
		this.output = output;
	}
	
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		output.println("getAcceptedIssuers returns null");
		return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
		for (X509Certificate cert : certs)
			output.println("checkClientTrusted: " + cert.toString());
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
		for (X509Certificate cert : certs)
			output.println("checkServerTrusted: " + cert.toString());
	}
}
