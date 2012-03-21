import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPSClient {
	// Create a trust manager that does not validate certificate chains
	public static void main(String[] args) throws Exception {
		LoggingTrustManager.register(System.out);

		String httpsURL = args[0];
		URL myurl = new URL(httpsURL);
		System.out.println("opening connection to: <" + args[0] + ">");
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
