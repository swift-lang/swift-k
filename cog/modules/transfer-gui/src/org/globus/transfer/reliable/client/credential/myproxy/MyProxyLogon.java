/*
 * Copyright 2007 The Board of Trustees of the University of Illinois.
 * All rights reserved.
 * 
 * Developed by:
 * 
 *   MyProxy Team
 *   National Center for Supercomputing Applications
 *   University of Illinois
 *   http://myproxy.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 *   Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * 
 *   Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * 
 *   Neither the names of the National Center for Supercomputing
 *   Applications, the University of Illinois, nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */
package org.globus.transfer.reliable.client.credential.myproxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

/**
 * The MyProxyLogon class provides an interface for retrieving credentials from
 * a MyProxy server.
 * <p>
 * First, use <code>setHost</code>, <code>setPort</code>,
 * <code>setUsername</code>, <code>setPassphrase</code>,
 * <code>setCredentialName</code>, <code>setLifetime</code> and
 * <code>requestTrustRoots</code> to configure. Then call <code>connect</code>,
 * <code>logon</code>, <code>getCredentials</code>, then
 * <code>disconnect</code>. Use <code>getCertificates</code> and
 * <code>getPrivateKey</code> to access the retrieved credentials, or
 * <code>writeProxyFile</code> or <code>saveCredentialsToFile</code> to
 * write them to a file. Use <code>writeTrustRoots</code>,
 * <code>getTrustedCAs</code>, <code>getCRLs</code>,
 * <code>getTrustRootData</code>, and <code>getTrustRootFilenames</code>
 * for trust root information.
 * @version 1.0
 * @see <a href="http://myproxy.ncsa.uiuc.edu/">MyProxy Project Home Page</a>
 */
public class MyProxyLogon {
    static Logger logger = Logger.getLogger(MyProxyLogon.class.getName());
    public final static String version = "1.0";
    public final static String BouncyCastleLicense = org.bouncycastle.LICENSE.licenseText;

    protected enum State {
	READY, CONNECTED, LOGGEDON, DONE
    }

    private class MyTrustManager implements X509TrustManager {
	public X509Certificate[] getAcceptedIssuers() {
	    X509Certificate[] issuers = null;
	    String certDirPath = MyProxyLogon.getExistingTrustRootPath();
	    if (certDirPath == null) {
		return null;
	    }
	    File dir = new File(certDirPath);
	    if (!dir.isDirectory()) {
		return null;
	    }
	    String[] certFilenames = dir.list();
	    String[] certData = new String[certFilenames.length];
	    for (int i = 0; i < certFilenames.length; i++) {
		try {
		    FileInputStream fileStream = new FileInputStream(
			    certDirPath + File.separator + certFilenames[i]);
		    byte[] buffer = new byte[fileStream.available()];
		    fileStream.read(buffer);
		    certData[i] = new String(buffer);
		} catch (Exception e) {
		    // ignore
		}
	    }
	    try {
		issuers = getX509CertsFromStringList(certData, certFilenames);
	    } catch (Exception e) {
		// ignore
	    }
	    return issuers;
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType)
	throws CertificateException {
	    throw new CertificateException(
	    "checkClientTrusted not implemented by edu.uiuc.ncsa.MyProxy.MyProxyLogon.MyTrustManager");
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType)
	throws CertificateException {
	    checkServerCertPath(certs);
	    checkServerDN(certs[0]);
	}

	private void checkServerCertPath(X509Certificate[] certs)
	throws CertificateException {
	    try {
		CertPathValidator validator = CertPathValidator
		.getInstance(CertPathValidator.getDefaultType());
		CertificateFactory certFactory = CertificateFactory
		.getInstance("X.509");
		CertPath certPath = certFactory.generateCertPath(Arrays
			.asList(certs));
		X509Certificate[] acceptedIssuers = getAcceptedIssuers();
		if (acceptedIssuers == null) {
		    String certDir = MyProxyLogon.getExistingTrustRootPath();
		    if (certDir != null) {
			throw new CertificateException(
				"no CA certificates found in " + certDir);
		    } else if (!requestTrustRoots) {
			throw new CertificateException(
			"no CA certificates directory found");
		    }
		    logger
		    .info("no trusted CAs configured -- bootstrapping trust from MyProxy server");
		    acceptedIssuers = new X509Certificate[1];
		    acceptedIssuers[0] = certs[certs.length - 1];
		}
		Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>(
			acceptedIssuers.length);
		for (int i = 0; i < acceptedIssuers.length; i++) {
		    TrustAnchor ta = new TrustAnchor(acceptedIssuers[i], null);
		    trustAnchors.add(ta);
		}
		PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
		pkixParameters.setRevocationEnabled(false);
		validator.validate(certPath, pkixParameters);
	    } catch (CertificateException e) {
		throw e;
	    } catch (GeneralSecurityException e) {
		throw new CertificateException(e);
	    }
	}

	private void checkServerDN(X509Certificate cert)
	throws CertificateException {
	    String subject = cert.getSubjectX500Principal().getName();
	    logger.fine("MyProxy server DN: " + subject);
	    int index = subject.indexOf("CN=");
	    if (index == -1) {
		throw new CertificateException("Server certificate subject ("
			+ subject + "does not contain a CN component.");
	    }
	    String CN = subject.substring(index + 3);
	    index = CN.indexOf(',');
	    if (index >= 0) {
		CN = CN.substring(0, index);
	    }
	    if ((index = CN.indexOf('/')) >= 0) {
		String service = CN.substring(0, index);
		CN = CN.substring(index + 1);
		if (!service.equals("host") && !service.equals("myproxy")) {
		    throw new CertificateException(
			    "Server certificate subject CN contains unknown service element: "
			    + subject);
		}
	    }
	    String myHostname = host;
	    if (myHostname.equals("localhost")) {
		try {
		    myHostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		    // ignore
		}
	    }
	    if (!CN.equals(myHostname)) {
		throw new CertificateException(
			"Server certificate subject CN (" + CN
			+ ") does not match server hostname (" + host
			+ ").");
	    }
	}
    }

    private final static int b64linelen = 64;
    private final static String X509_USER_PROXY_FILE = "x509up_u";
    private final static String VERSION = "VERSION=MYPROXYv2";
    private final static String GETCOMMAND = "COMMAND=0";
    private final static String TRUSTROOTS = "TRUSTED_CERTS=";
    private final static String USERNAME = "USERNAME=";
    private final static String PASSPHRASE = "PASSPHRASE=";
    private final static String LIFETIME = "LIFETIME=";
    private final static String CREDNAME = "CRED_NAME=";
    private final static String RESPONSE = "RESPONSE=";
    private final static String ERROR = "ERROR=";
    private final static String DN = "CN=ignore";
    private final static String TRUSTED_CERT_PATH = "/.globus/certificates";

    protected final static int keySize = 1024;
    protected final int MIN_PASS_PHRASE_LEN = 6;
    protected final static String keyAlg = "RSA";
    protected final static String pkcs10SigAlgName = "SHA1withRSA";
    protected final static String pkcs10Provider = "SunRsaSign";
    protected State state = State.READY;
    protected String host = "localhost";
    protected String username;
    protected String credname;
    protected String passphrase;
    protected int port = 7512;
    protected int lifetime = 43200;
    protected boolean requestTrustRoots = false;
    protected SSLSocket socket;
    protected BufferedInputStream socketIn;
    protected BufferedOutputStream socketOut;
    protected KeyPair keypair;
    protected Collection certificateChain;
    protected String[] trustrootFilenames;
    protected String[] trustrootData;

    /**
     * Constructs a MyProxyLogon object.
     */
    public MyProxyLogon() {
	super();
	host = System.getenv("MYPROXY_SERVER");
	if (host == null) {
	    host = "localhost";
	}
	String portString = System.getenv("MYPROXY_SERVER_PORT");
	if (portString != null) {
	    port = Integer.parseInt(portString);
	}
	username = System.getProperty("user.name");
    }

    /**
     * Gets the hostname of the MyProxy server.
     * @return MyProxy server hostname
     */
    public String getHost() {
	return this.host;
    }

    /**
     * Sets the hostname of the MyProxy server. Defaults to localhost.
     * @param host
     *        MyProxy server hostname
     */
    public void setHost(String host) {
	this.host = host;
    }

    /**
     * Gets the port of the MyProxy server.
     * @return MyProxy server port
     */
    public int getPort() {
	return this.port;
    }

    /**
     * Sets the port of the MyProxy server. Defaults to 7512.
     * @param port
     *        MyProxy server port
     */
    public void setPort(int port) {
	this.port = port;
    }

    /**
     * Gets the MyProxy username.
     * @return MyProxy server port
     */
    public String getUsername() {
	return this.username;
    }

    /**
     * Sets the MyProxy username. Defaults to user.name.
     * @param username
     *        MyProxy username
     */
    public void setUsername(String username) {
	this.username = username;
    }

    /**
     * Gets the optional MyProxy credential name.
     * @return credential name
     */
    public String getCredentialName() {
	return this.credname;
    }

    /**
     * Sets the optional MyProxy credential name.
     * @param credname
     *        credential name
     */
    public void setCredentialName(String credname) {
	this.credname = credname;
    }

    /**
     * Sets the MyProxy passphrase.
     * @param passphrase
     *        MyProxy passphrase
     */
    public void setPassphrase(String passphrase) {
	this.passphrase = passphrase;
    }

    /**
     * Gets the requested credential lifetime.
     * @return Credential lifetime
     */
    public int getLifetime() {
	return this.lifetime;
    }

    /**
     * Sets the requested credential lifetime. Defaults to 43200 seconds (12
     * hours).
     * @param seconds
     *        Credential lifetime
     */
    public void setLifetime(int seconds) {
	this.lifetime = seconds;
    }

    /**
     * Gets the certificates returned from the MyProxy server by
     * getCredentials().
     * @return Collection of java.security.cert.Certificate objects
     */
    public Collection getCertificates() {
	return this.certificateChain;
    }

    /**
     * Gets the private key generated by getCredentials().
     * @return PrivateKey
     */
    public PrivateKey getPrivateKey() {
	return this.keypair.getPrivate();
    }

    /**
     * Sets whether to request trust roots (CA certificates, CRLs, signing
     * policy files) from the MyProxy server. Defaults to false (i.e., not
     * to request trust roots).
     * @param flag
     *        If true, request trust roots. If false, don't request trust
     *        roots.
     */
    public void requestTrustRoots(boolean flag) {
	this.requestTrustRoots = flag;
    }

    /**
     * Gets trust root filenames.
     * @return trust root filenames
     */
    public String[] getTrustRootFilenames() {
	return this.trustrootFilenames;
    }

    /**
     * Gets trust root data corresponding to the trust root filenames.
     * @return trust root data
     */
    public String[] getTrustRootData() {
	return this.trustrootData;
    }

    /**
     * Connects to the MyProxy server at the desired host and port. Requires
     * host authentication via SSL. The host's certificate subject must
     * match the requested hostname. If CA certificates are found in the
     * standard GSI locations, they will be used to verify the server's
     * certificate. If trust roots are requested and no CA certificates are
     * found, the server's certificate will still be accepted.
     */
    public void connect() throws IOException, GeneralSecurityException {
	SSLContext sc = SSLContext.getInstance("SSL");
	TrustManager[] trustAllCerts = new TrustManager[] { new MyTrustManager() };
	sc.init(null, trustAllCerts, new java.security.SecureRandom());
	SSLSocketFactory sf = sc.getSocketFactory();
	this.socket = (SSLSocket) sf.createSocket(this.host, this.port);
	this.socket.setEnabledProtocols(new String[] { "SSLv3" });
	this.socket.startHandshake();
	this.socketIn = new BufferedInputStream(this.socket.getInputStream());
	this.socketOut = new BufferedOutputStream(this.socket.getOutputStream());
	this.state = State.CONNECTED;
    }

    /**
     * Disconnects from the MyProxy server.
     */
    public void disconnect() throws IOException {
	this.socket.close();
	this.socket = null;
	this.socketIn = null;
	this.socketOut = null;
	this.state = State.READY;
    }

    /**
     * Logs on to the MyProxy server by issuing the MyProxy GET command.
     */
    public void logon() throws IOException, GeneralSecurityException {
	String line;
	char response;

	if (this.state != State.CONNECTED) {
	    this.connect();
	}

	this.socketOut.write('0');
	this.socketOut.flush();
	this.socketOut.write(VERSION.getBytes());
	this.socketOut.write('\n');
	this.socketOut.write(GETCOMMAND.getBytes());
	this.socketOut.write('\n');
	this.socketOut.write(USERNAME.getBytes());
	this.socketOut.write(this.username.getBytes());
	this.socketOut.write('\n');
	this.socketOut.write(PASSPHRASE.getBytes());
	this.socketOut.write(this.passphrase.getBytes());
	this.socketOut.write('\n');
	this.socketOut.write(LIFETIME.getBytes());
	this.socketOut.write(Integer.toString(this.lifetime).getBytes());
	this.socketOut.write('\n');
	if (this.credname != null) {
	    this.socketOut.write(CREDNAME.getBytes());
	    this.socketOut.write(this.credname.getBytes());
	    this.socketOut.write('\n');
	}
	if (this.requestTrustRoots) {
	    this.socketOut.write(TRUSTROOTS.getBytes());
	    this.socketOut.write("1\n".getBytes());
	}
	this.socketOut.flush();

	line = readLine(this.socketIn);
	if (line == null) {
	    throw new EOFException();
	}
	if (!line.equals(VERSION)) {
	    throw new ProtocolException("bad MyProxy protocol VERSION string: "
		    + line);
	}
	line = readLine(this.socketIn);
	if (line == null) {
	    throw new EOFException();
	}
	if (!line.startsWith(RESPONSE)
		|| line.length() != RESPONSE.length() + 1) {
	    throw new ProtocolException(
		    "bad MyProxy protocol RESPONSE string: " + line);
	}
	response = line.charAt(RESPONSE.length());
	if (response == '1') {
	    StringBuffer errString;

	    errString = new StringBuffer("MyProxy logon failed");
	    while ((line = readLine(this.socketIn)) != null) {
		if (line.startsWith(ERROR)) {
		    errString.append('\n');
		    errString.append(line.substring(ERROR.length()));
		}
	    }
	    throw new FailedLoginException(errString.toString());
	} else if (response == '2') {
	    throw new ProtocolException(
	    "MyProxy authorization RESPONSE not implemented");
	} else if (response != '0') {
	    throw new ProtocolException(
		    "unknown MyProxy protocol RESPONSE string: " + line);
	}
	while ((line = readLine(this.socketIn)) != null) {
	    if (line.startsWith(TRUSTROOTS)) {
		String filenameList = line.substring(TRUSTROOTS.length());
		this.trustrootFilenames = filenameList.split(",");
		this.trustrootData = new String[this.trustrootFilenames.length];
		for (int i = 0; i < this.trustrootFilenames.length; i++) {
		    String lineStart = "FILEDATA_" + this.trustrootFilenames[i]
		                                                             + "=";
		    line = readLine(this.socketIn);
		    if (line == null) {
			throw new EOFException();
		    }
		    if (!line.startsWith(lineStart)) {
			throw new ProtocolException(
				"bad MyProxy protocol RESPONSE: expecting "
				+ lineStart + " but received " + line);
		    }
		    this.trustrootData[i] = new String(Base64.decode(line
			    .substring(lineStart.length())));
		}
	    }
	}
	this.state = State.LOGGEDON;
    }

    /**
     * Retrieves credentials from the MyProxy server.
     */
    public void getCredentials() throws IOException, GeneralSecurityException {
	int numCertificates;
	KeyPairGenerator keyGenerator;
	PKCS10CertificationRequest pkcs10;
	CertificateFactory certFactory;

	if (this.state != State.LOGGEDON) {
	    this.logon();
	}

	keyGenerator = KeyPairGenerator.getInstance(keyAlg);
	keyGenerator.initialize(keySize);
	this.keypair = keyGenerator.genKeyPair();
	pkcs10 = new PKCS10CertificationRequest(pkcs10SigAlgName,
		new X500Principal(DN), this.keypair.getPublic(), null,
		this.keypair.getPrivate(), pkcs10Provider);
	this.socketOut.write(pkcs10.getEncoded());
	this.socketOut.flush();
	numCertificates = this.socketIn.read();
	if (numCertificates == -1) {
	    System.err.println("connection aborted");
	    System.exit(1);
	} else if (numCertificates == 0 || numCertificates < 0) {
	    System.err.print("bad number of certificates sent by server: ");
	    System.err.println(Integer.toString(numCertificates));
	    System.exit(1);
	}
	certFactory = CertificateFactory.getInstance("X.509");
	this.certificateChain = certFactory.generateCertificates(this.socketIn);
	this.state = State.DONE;
    }

    /**
     * Writes the retrieved credentials to the Globus proxy file location.
     */
    public void writeProxyFile() throws IOException, GeneralSecurityException {
	saveCredentialsToFile(getProxyLocation());
    }

    /**
     * Writes the retrieved credentials to the specified filename.
     */
    public void saveCredentialsToFile(String filename) throws IOException,
    GeneralSecurityException {
	Iterator iter;
	X509Certificate certificate;
	PrintStream printStream;

	iter = this.certificateChain.iterator();
	certificate = (X509Certificate) iter.next();
	File outFile = new File(filename);
	outFile.delete();
	outFile.createNewFile();
	setFilePermissions(filename, "0600");
	printStream = new PrintStream(new FileOutputStream(outFile));
	printCert(certificate, printStream);
	printKey(keypair.getPrivate(), printStream);
	while (iter.hasNext()) {
	    certificate = (X509Certificate) iter.next();
	    printCert(certificate, printStream);
	}
    }

    /**
     * Writes the retrieved trust roots to the Globus trusted certificates
     * directory.
     * @return true if trust roots are written successfully, false if no
     *         trust roots are available to be written
     */
    public boolean writeTrustRoots() throws IOException {
	return writeTrustRoots(getTrustRootPath());
    }

    /**
     * Writes the retrieved trust roots to a trusted certificates directory.
     * @param directory
     *        path where the trust roots should be written
     * @return true if trust roots are written successfully, false if no
     *         trust roots are available to be written
     */
    public boolean writeTrustRoots(String directory) throws IOException {
	if (this.trustrootFilenames == null || this.trustrootData == null) {
	    return false;
	}
	File rootDir = new File(directory);
	if (!rootDir.exists()) {
	    rootDir.mkdirs();
	}
	for (int i = 0; i < trustrootFilenames.length; i++) {
	    FileOutputStream out = new FileOutputStream(directory
		    + File.separator + this.trustrootFilenames[i]);
	    out.write(this.trustrootData[i].getBytes());
	    out.close();
	}
	return true;
    }

    /**
     * Gets the trusted CA certificates returned by the MyProxy server.
     * @return trusted CA certificates, or null if none available
     */
    public X509Certificate[] getTrustedCAs() throws CertificateException {
	if (trustrootData == null)
	    return null;
	return getX509CertsFromStringList(trustrootData, trustrootFilenames);
    }

    private static X509Certificate[] getX509CertsFromStringList(
	    String[] certList, String[] nameList) throws CertificateException {
	CertificateFactory certFactory = CertificateFactory
	.getInstance("X.509");
	Collection<X509Certificate> c = new ArrayList<X509Certificate>(
		certList.length);
	for (int i = 0; i < certList.length; i++) {
	    int index = -1;
	    String certData = certList[i];
	    if (certData != null) {
		index = certData.indexOf("-----BEGIN CERTIFICATE-----");
	    }
	    if (index >= 0) {
		certData = certData.substring(index);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
			certData.getBytes());
		try {
		    X509Certificate cert = (X509Certificate) certFactory
		    .generateCertificate(inputStream);
		    c.add(cert);
		} catch (Exception e) {
		    if (nameList != null) {
			logger.warning(nameList[i]
			                        + " can not be parsed as an X509Certificate.");
		    } else {
			logger.warning("failed to parse an X509Certificate");
		    }
		}
	    }
	}
	if (c.isEmpty())
	    return null;
	return c.toArray(new X509Certificate[0]);
    }

    /**
     * Gets the CRLs returned by the MyProxy server.
     * @return CRLs or null if none available
     */
    public X509CRL[] getCRLs() throws CertificateException {
	if (trustrootData == null)
	    return null;
	CertificateFactory certFactory = CertificateFactory
	.getInstance("X.509");
	Collection<X509CRL> c = new ArrayList<X509CRL>(trustrootData.length);
	for (int i = 0; i < trustrootData.length; i++) {
	    String crlData = trustrootData[i];
	    int index = crlData.indexOf("-----BEGIN X509 CRL-----");
	    if (index >= 0) {
		crlData = crlData.substring(index);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
			crlData.getBytes());
		try {
		    X509CRL crl = (X509CRL) certFactory
		    .generateCRL(inputStream);
		    c.add(crl);
		} catch (Exception e) {
		    logger.warning(this.trustrootFilenames[i]
		                                           + " can not be parsed as an X509CRL.");
		}
	    }
	}
	if (c.isEmpty())
	    return null;
	return c.toArray(new X509CRL[0]);
    }

    /**
     * Returns the trusted certificates directory location where
     * writeTrustRoots() will store certificates.
     */
    public static String getTrustRootPath() {
	String path;

	path = System.getenv("X509_CERT_DIR");
	if (path == null) {
	    path = System.getProperty("X509_CERT_DIR");
	}
	if (path == null) {
	    path = System.getProperty("user.home") + TRUSTED_CERT_PATH;
	}
	return path;
    }

    /**
     * Gets the existing trusted CA certificates directory.
     * @return directory path string or null if none found
     */
    public static String getExistingTrustRootPath() {
	String path, GL;

	GL = System.getenv("GLOBUS_LOCATION");
	if (GL == null) {
	    GL = System.getProperty("GLOBUS_LOCATION");
	}

	path = System.getenv("X509_CERT_DIR");
	if (path == null) {
	    path = System.getProperty("X509_CERT_DIR");
	}
	if (path == null) {
	    path = getDir(System.getProperty("user.home") + TRUSTED_CERT_PATH);
	}
	if (path == null) {
	    path = getDir("/etc/grid-security/certificates");
	}
	if (path == null) {
	    path = getDir(GL + File.separator + "share" + File.separator
		    + "certificates");
	}

	return path;
    }

    /**
     * Returns the default Globus proxy file location.
     */
    public static String getProxyLocation() throws IOException {
	String loc, suffix = null;
	Process proc;
	BufferedReader bufferedReader;

	loc = System.getenv("X509_USER_PROXY");
	if (loc == null) {
	    loc = System.getProperty("X509_USER_PROXY");
	}
	if (loc != null) {
	    return loc;
	}

	try {
	    proc = Runtime.getRuntime().exec("id -u");
	    bufferedReader = new BufferedReader(new InputStreamReader(proc
		    .getInputStream()));
	    suffix = bufferedReader.readLine();
	} catch (IOException e) {
	    // will fail on windows
	}

	if (suffix == null) {
	    suffix = System.getProperty("user.name");
	    if (suffix != null) {
		suffix = suffix.toLowerCase();
	    } else {
		suffix = "nousername";
	    }
	}
	String tmpdir = System.getProperty("java.io.tmpdir");
	if (!tmpdir.endsWith(File.separator)) {
		tmpdir += File.separator;
	}
	System.out.println("location");
	return tmpdir + X509_USER_PROXY_FILE + suffix;
    }

    /**
     * Provides a simple command-line interface.
     */
    public static void main(String[] args) {
	try {
	    MyProxyLogon m = new MyProxyLogon();
	    // Console cons = System.console();
	    String passphrase = null;
	    X509Certificate[] CAcerts;
	    X509CRL[] CRLs;
	    MyProxyLogon.logger.setLevel(Level.ALL);

	    // if (cons != null) {
	    // char[] pass = cons.readPassword("[%s]", "MyProxy Passphrase:
	    // ");
	    // if (pass != null) {
	    // passphrase = new String(pass);
	    // }
	    // } else {
	    System.out
	    .println("Warning: terminal will echo passphrase as you type.");
	    System.out.print("MyProxy Passphrase: ");
	    passphrase = readLine(System.in);
	    // }
	    if (passphrase == null) {
		System.err.println("Error reading passphrase.");
		System.exit(1);
	    }
	    m.setPassphrase(passphrase);
	    m.requestTrustRoots(true);
	    m.getCredentials();
	    m.writeProxyFile();
	    System.out.println("Credential written successfully.");
	    CAcerts = m.getTrustedCAs();
	    if (CAcerts != null) {
		System.out.println(Integer.toString(CAcerts.length)
			+ " CA certificates received.");
	    }
	    CRLs = m.getCRLs();
	    if (CRLs != null) {
		System.out.println(Integer.toString(CRLs.length)
			+ " CRLs received.");
	    }
	    if (m.writeTrustRoots()) {
		System.out.println("Wrote trust roots to "
			+ MyProxyLogon.getTrustRootPath() + ".");
	    } else {
		System.out
		.println("Received no trust roots from MyProxy server.");
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    private static void printB64(byte[] data, PrintStream out) {
	byte[] b64data;

	b64data = Base64.encode(data);
	for (int i = 0; i < b64data.length; i += b64linelen) {
	    if ((b64data.length - i) > b64linelen) {
		out.write(b64data, i, b64linelen);
	    } else {
		out.write(b64data, i, b64data.length - i);
	    }
	    out.println();
	}
    }

    private static void printCert(X509Certificate certificate, PrintStream out)
    throws CertificateEncodingException {
	out.println("-----BEGIN CERTIFICATE-----");
	printB64(certificate.getEncoded(), out);
	out.println("-----END CERTIFICATE-----");
    }

    private static void printKey(PrivateKey key, PrintStream out)
    throws IOException {
	out.println("-----BEGIN RSA PRIVATE KEY-----");
	ByteArrayInputStream inStream = new ByteArrayInputStream(key
		.getEncoded());
	ASN1InputStream derInputStream = new ASN1InputStream(inStream);
	DERObject keyInfo = derInputStream.readObject();
	PrivateKeyInfo pkey = new PrivateKeyInfo((ASN1Sequence) keyInfo);
	DERObject derKey = pkey.getPrivateKey();
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	DEROutputStream der = new DEROutputStream(bout);
	der.writeObject(derKey);
	printB64(bout.toByteArray(), out);
	out.println("-----END RSA PRIVATE KEY-----");
    }

    private static void setFilePermissions(String file, String mode) {
	String command = "chmod " + mode + " " + file;
	try {
	    Runtime.getRuntime().exec(command);
	} catch (IOException e) {
	    logger.warning("Failed to run: " + command); // windows
	}
    }

    private static String readLine(InputStream is) throws IOException {
	StringBuffer sb = new StringBuffer();
	for (int c = is.read(); c > 0 && c != '\n'; c = is.read()) {
	    sb.append((char) c);
	}
	if (sb.length() > 0) {
	    return new String(sb);
	}
	return null;
    }

    private static String getDir(String path) {
	if (path == null)
	    return null;
	File f = new File(path);
	if (f.isDirectory() && f.canRead()) {
	    return f.getAbsolutePath();
	}
	return null;
    }
}
