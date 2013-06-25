import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Cert {

	public static void main(String[] args) throws IOException,
			GeneralSecurityException {

		listDefaultTrustManager();
		listSecurityProviders();
		KeyPair keyPair = generateKeyPair();
		generateSignature(keyPair.getPrivate());
		loadX509Cert("C:/certs/ca.crt");
		loadX509Cert("C:/certs/ca.crt.der");
		loadPrivateKey(readRaw("C:/certs/ca.key.pk8"),
				"caKeyPasswd".toCharArray());
		loadPrivateKey(readPEM("C:/certs/ca.key.pem"),
				"caKeyPasswd".toCharArray());
		// loadPkcs12Keystore("C:/certs/ca.key.pk12", null);
		// listWindowsRoot();
	}

	public static void generateSignature(PrivateKey priv)
			throws NoSuchAlgorithmException, InvalidKeyException,
			InvalidAlgorithmParameterException {
		Signature rsa = Signature.getInstance("SHA1withRSA");
		rsa.initSign(priv);
		// rsa.setParameter(AlgorithmParameterSpec)
		System.out.println("rsa: " + rsa.toString());
	}

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException,
			NoSuchProviderException {
		System.out.println("Generate Keypair:");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey priv = pair.getPrivate();
		PublicKey pub = pair.getPublic();
		System.out.println("  private key: " + priv.toString());
		System.out.println("  pub key: " + pub.toString());
		return pair;
	}

	public static void listSecurityProviders() {
		System.out.println("***** Security Providers:");
		for (Provider p : Security.getProviders())
			System.out.println("  " + p.toString());
	}

	public static void listWindowsRoot() throws GeneralSecurityException,
			IOException {
		System.out.println("***** Listing Windows Roots:");
		KeyStore ks = KeyStore.getInstance("Windows-ROOT");
		ks.load(null, null);
		java.util.Enumeration en = ks.aliases();

		while (en.hasMoreElements()) {
			String aliasKey = (String) en.nextElement();
			Certificate c = ks.getCertificate(aliasKey);
			System.out.println("---> alias : " + aliasKey);
			System.out.println("    Certificat : " + c.toString());

			if (aliasKey.equals("myKey")) {
				PrivateKey key = (PrivateKey) ks.getKey(aliasKey,
						"monPassword".toCharArray());
				Certificate[] chain = ks.getCertificateChain(aliasKey);
			}
		}
	}

	public static void loadX509Cert(String path)
			throws GeneralSecurityException, IOException, IOException {
		System.out.println("***** Loading X.509 certificate from file " + path);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				path));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		while (bis.available() > 0) {
			Certificate cert = cf.generateCertificate(bis);
			System.out.println(" " + cert.toString());
		}
	}

	public static void loadPkcs12Keystore(String path, char[] password)
			throws GeneralSecurityException, IOException, IOException {
		System.out.println("***** Loading pkcs12 keystore from file " + path);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(path), password);
		for (Enumeration<String> aliases = ks.aliases(); aliases
				.hasMoreElements();) {
			Entry entry = ks.getEntry(aliases.nextElement(), null);
			System.out.println("Entry: " + entry.toString());
		}
	}

	public static byte[] readRaw(String path) throws IOException {
		System.out.println("***** Loading bytes from file " + path);
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		fis.read(buffer);
		return buffer;
	}

	public static void loadPrivateKey(byte[] data, char[] password)
			throws IOException, GeneralSecurityException {

		System.out.println("***** Loading private key from in memory buffer");
		System.out.println("Input length: " + data.length + " byte");
		byte b = ' ';
		for (int i = 0; i < data.length && i < 5; b = data[i++])
			System.out.print(Integer.toHexString(b) + " ");
		System.out.println();

		EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(data);
		System.out.println("Encrypted private key info's algorithm name : "
				+ epki.getAlgName());

		PBEKeySpec keySpec = new PBEKeySpec(password);
		SecretKey encryptedKey = SecretKeyFactory
				.getInstance(epki.getAlgName()).generateSecret(keySpec);
		System.out.println("Key algorithm: " + encryptedKey.getAlgorithm());
		System.out.println("Key format: " + encryptedKey.getFormat());

		byte[] rawstream = null;
		Cipher cipher = Cipher.getInstance(epki.getAlgName());
		cipher.init(Cipher.DECRYPT_MODE, encryptedKey, epki.getAlgParameters());
		rawstream = cipher.doFinal(epki.getEncryptedData());
		System.out.println("Output length: " + rawstream.length + " byte");
		for (byte i : rawstream)
			System.out.print(Integer.toHexString(i) + " ");
		System.out.println();
		keySpec.clearPassword();
	}

	private static byte[] readPEM(String path) throws IOException {
		System.out.println("***** Loading bytes from PEM file " + path);
		InputStream is = new FileInputStream(path);
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is,
				"ISO-8859-1"));
		final int NOT_STARTED = 0;
		final int RUNNING = 1;
		final int DONE = 2;
		int readingState = NOT_STARTED;
		StringBuffer keyBase64DataBuffer = new StringBuffer();
		{
			String line;
			while ((line = lnr.readLine()) != null && readingState != DONE) {
				if (readingState == NOT_STARTED) {
					if (line.startsWith("-----BEGIN RSA PRIVATE KEY-----")) {
						readingState = RUNNING;
					}
				} else {
					// readingState == RUNNING
					if (line.startsWith("-----END RSA PRIVATE KEY-----")) {
						readingState = DONE;
					} else {
						keyBase64DataBuffer.append(line);
					}
				}
			}
		}
		char[] carray = keyBase64DataBuffer.toString().toCharArray();
		byte[] barray = new byte[carray.length];
		for (int i = 0; i < carray.length; i++) {
			barray[i] = (byte) carray[i];
		}
		return javax.xml.bind.DatatypeConverter
				.parseBase64Binary(keyBase64DataBuffer.toString());
	}

	private static void listDefaultTrustManager() throws NoSuchAlgorithmException,
			KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		trustManagerFactory.init((KeyStore) null);

		System.out.println("JVM Default Trust Managers:");
		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
			System.out.println(trustManager);

			if (trustManager instanceof X509TrustManager) {
				X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
				System.out.println("\tAccepted issuers count : "
						+ x509TrustManager.getAcceptedIssuers().length);
				for (X509Certificate cert : x509TrustManager.getAcceptedIssuers())
					System.out.println("cert: subject:"+cert.getSubjectDN()+", issuer:"+cert.getIssuerDN()+", notAfter:"+cert.getNotAfter()+", notBefore:"+cert.getNotBefore());
			}
		}
	}
}
