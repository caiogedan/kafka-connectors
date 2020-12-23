package pt.isel.tfm.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLUtils {
	private static final Logger log = LoggerFactory.getLogger(SSLUtils.class);

	private String caCrtFile;
	private String crtFile;
	private String keyFile;
	// private final String password = generateKeystorePassword();
	private final String password = "";

	public SSLUtils(String caCrtFile, String crtFile, String keyFile) {
		this.caCrtFile = caCrtFile;
		this.crtFile = crtFile;
		this.keyFile = keyFile;
	}

	private static String generateKeystorePassword() {
		String password = "";
		int passwordLength = 10;
		for (int i = 0; i < passwordLength; i++) {
			int asciiGroup = ThreadLocalRandom.current().nextInt(0, 3);
			switch (asciiGroup) {
			case 0:
				// Picking a random numerical character
				password += (char) ThreadLocalRandom.current().nextInt(48, 58);
			case 1:
				// Picking a random uppercase letter
				password += (char) ThreadLocalRandom.current().nextInt(65, 91);
			case 2:
				// Picking a random lowercase letter
				password += (char) ThreadLocalRandom.current().nextInt(97, 123);
			}
		}
		return password;
	}

	public SSLSocketFactory getMqttSocketFactory() throws IOException {

		Security.addProvider(new BouncyCastleProvider());

		String PROVIDER = "BC";

		/*
		 * LOCA CA CERTIFICATE
		 */

		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = null;

		try {
			cf = CertificateFactory.getInstance("X.509", PROVIDER);
		} catch (CertificateException | NoSuchProviderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (bis.available() > 0) {
			try {
				caCert = (X509Certificate) cf.generateCertificate(bis);
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * LOCA CLIENT CERTIFICATE
		 */
		bis = new BufferedInputStream(new FileInputStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			try {
				cert = (X509Certificate) cf.generateCertificate(bis);
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * LOAD CLIENT PRIVATE KEY
		 */

		PEMParser pemParser = new PEMParser(new FileReader(keyFile));
		Object object = pemParser.readObject();
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(PROVIDER);
		KeyPair key;
		if (object instanceof PEMEncryptedKeyPair) {
			log.debug("Encrypted key - we will use provided password");
			key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
		} else {
			log.debug("Unencrypted key - no password needed");
			key = converter.getKeyPair((PEMKeyPair) object);
		}
		pemParser.close();

		/*
		 * CA CERTIFICATE IS USED TO AUTHENTICATE SERVER
		 */
		TrustManagerFactory tmf = null;
		KeyStore caKs;
		try {
			caKs = KeyStore.getInstance(KeyStore.getDefaultType());
			caKs.load(null, null);
			caKs.setCertificateEntry("ca-certificate", caCert);
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(caKs);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * CLIENT KEY AND CERTIFICATE ARE SENT TO SERVER SO IT CAN AUTHENTICATE US
		 */

		KeyStore ks;
		KeyManagerFactory kmf = null;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry("certificate", cert);
			ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
					new java.security.cert.Certificate[] { cert });
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password.toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// finally, create SSL socket factory
		SSLContext context = null;
		try {
			context = SSLContext.getInstance("TLSv1.2");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return context.getSocketFactory();
	}
}
