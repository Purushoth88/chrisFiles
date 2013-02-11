import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Reads from a https connection and print data sent during authentication
public class DebugHttpsClient {
	public static void main(String[] args) throws Exception {
		// For debugging: register a TrustManager which prints out everything
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				System.out.println("getAcceptedIssuers returns null");
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				for (X509Certificate cert : certs)
					System.out.println("checkClientTrusted: " + cert.toString());
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				for (X509Certificate cert : certs)
					System.out.println("checkServerTrusted: " + cert.toString());
			}

		} }, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		URL myurl = new URL(args.length>0 ? args[0] : "https://raw.github.com/chalstrick/chrisFiles/master/README");
		System.out.println("opening connection to: <" + myurl.toString() + ">");
		HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
		InputStream ins = con.getInputStream();
		InputStreamReader isr = new InputStreamReader(ins);
		BufferedReader in = new BufferedReader(isr);

		String inputLine;

		System.out.println("start reading from connection:");

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}

		System.out.println("finished reading from connection:");
		in.close();
	}
}