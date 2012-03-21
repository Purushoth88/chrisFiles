import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class HTTPSServer {
	public static String keyStoreType = "JKS";
	
	// Create a trust manager that does not validate certificate chains
	public static void main(String[] args) throws Exception {
		LoggingTrustManager.register(System.out);

		String ksName = "herong.jks";
		char ksPass[] = "HerongJKS".toCharArray();
		char ctPass[] = "My1stKey".toCharArray();
		try {
			KeyStore ks = KeyStore.getInstance(keyStoreType);

//			ks.load(new FileInputStream(ksName), ksPass);
			ks.load(null, ksPass);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(8888);
			System.out.println("Server started:");
			printServerSocketInfo(s);
			// Listening to the port
			SSLSocket c = (SSLSocket) s.accept();
			printSocketInfo(c);
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
					c.getOutputStream()));
			BufferedReader r = new BufferedReader(new InputStreamReader(
					c.getInputStream()));
			String m = r.readLine();
			w.write("HTTP/1.0 200 OK");
			w.newLine();
			w.write("Content-Type: text/html");
			w.newLine();
			w.newLine();
			w.write("<html><body>Hello world!</body></html>");
			w.newLine();
			w.flush();
			w.close();
			r.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public KeyStore createKeyStore(char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
	    KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(null, password);
        return ks;
	}

	private static void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: " + s.getClass());
		System.out.println("   Remote address = "
				+ s.getInetAddress().toString());
		System.out.println("   Remote port = " + s.getPort());
		System.out.println("   Local socket address = "
				+ s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+ s.getLocalAddress().toString());
		System.out.println("   Local port = " + s.getLocalPort());
		System.out.println("   Need client authentication = "
				+ s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = " + ss.getCipherSuite());
		System.out.println("   Protocol = " + ss.getProtocol());
	}

	private static void printServerSocketInfo(SSLServerSocket s) {
		System.out.println("Server socket class: " + s.getClass());
		System.out.println("   Socker address = "
				+ s.getInetAddress().toString());
		System.out.println("   Socker port = " + s.getLocalPort());
		System.out.println("   Need client authentication = "
				+ s.getNeedClientAuth());
		System.out.println("   Want client authentication = "
				+ s.getWantClientAuth());
		System.out.println("   Use client mode = " + s.getUseClientMode());
	}
}
