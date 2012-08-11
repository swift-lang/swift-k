//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 5, 2012
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.globus.cog.util.concurrent.FileLock;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.util.Util;

/**
 * A class to automatically generate:
 * - a CA key/cert pair
 * - a user key/cert pair signed by the above CA
 * - a proxy certificate signed by the above user key
 * 
 * This is heavily inspired by http://code.google.com/p/java-simple-ca/
 *
 */
public class AutoCA {
    public static final Logger logger = Logger.getLogger(AutoCA.class);
    
    public static final String CA_DIR = System.getProperty("user.home") + File.separator 
    + ".globus" + File.separator + "coasters";
    public static final String CA_CRT_NAME_PREFIX = "CAcert";
    public static final String CA_KEY_NAME_PREFIX = "CAkey";
    public static final String USER_CRT_NAME_PREFIX = "usercert";
    public static final String USER_KEY_NAME_PREFIX = "userkey";
    public static final String PROXY_NAME_PREFIX = "proxy";
    public static final String SIGNING_POLICY_RES_NAME = "autoCA.signing_policy";
    
    public static final String CA_CERT_ALGORITHM = "RSA";
    public static final int CA_CERT_BITS = 1024;
    public static final String CA_CERT_DN = "C=US,O=JavaCoG,OU=AutoCA,CN=Certificate Authority";
    public static final String USER_CERT_DN = "C=US,O=JavaCoG,OU=AutoCA,CN=User";
    public static final String CA_CERT_SIGNATURE_ALGORITHM = "SHA1WithRSAEncryption";
    
    public static final long WEEK_IN_MS = 1000 * 3600 * 24 * 7;
    public static final long CA_CERT_LIFETIME = 2 * WEEK_IN_MS;
    public static final long MIN_CA_CERT_LIFETIME_LEFT = WEEK_IN_MS;
    
    public static final int ID_BYTES = 4;
    
    private static AutoCA instance;
    private Info info;
    private X509Certificate cert;
    private X509V3CertificateGenerator gen;

    public synchronized static AutoCA getInstance() {
        if (instance == null) {
            instance = new AutoCA();
        }
        return instance;
    }
    
    public AutoCA() {
         gen = new X509V3CertificateGenerator();
    }
    
    public static class Info {
        public final String proxyPath, caCertPath;
        
        public Info(File proxyFile, File caCertFile) {
            this(proxyFile.getAbsolutePath(), caCertFile.getAbsolutePath());
        }
        
        public Info(String proxyPath, String caCertPath) {
            this.proxyPath = proxyPath;
            this.caCertPath = caCertPath;
        }
    }

    public Info createProxy() throws IOException, GeneralSecurityException {
        ensureCACertsExist();
        return info;
    }

    private void ensureCACertsExist() throws IOException, GeneralSecurityException {
        // delete expired CAs, make a new one if the existing ones don't have
        // at least MIN_CA_LIFETIME_LEFT
        FileLock fl = new FileLock(CA_DIR);
        try {
            fl.lock();
        }
        catch (Exception e) {
            logger.warn("Failed to lock CA dir", e);
        }
        try {
            File[] certs = discoverProxies();
            long now = System.currentTimeMillis();
            long maxExpirationTime = 0;
            
            for (File c : certs) {
                if (logger.isInfoEnabled()) {
                    logger.info("Checking certificate " + c);
                }
                X509Certificate cert = CertUtil.loadCertificate(c.getAbsolutePath());
                long certExpirationTime = cert.getNotAfter().getTime();
                if (certExpirationTime < now) {
                    // delete cert and key
                    if (logger.isInfoEnabled()) {
                        logger.info("Certificate expired. Deleting.");
                    }
                    deleteAll(getIndex(c));
                }
                if (certExpirationTime > maxExpirationTime) {
                    maxExpirationTime = certExpirationTime;
                    int index = getIndex(c);
                    this.info = new Info(makeFile(PROXY_NAME_PREFIX, index), makeFile(CA_CRT_NAME_PREFIX, index));
                    this.cert = cert;
                }
            }
            
            if (now + MIN_CA_CERT_LIFETIME_LEFT > maxExpirationTime) {
                int index = discoverNextIndex();
                this.info = new Info(makeFile(PROXY_NAME_PREFIX, index), makeFile(CA_CRT_NAME_PREFIX, index));
                if (logger.isInfoEnabled()) {
                    logger.info("No certificates with enough lifetime. Creating new certificate: " + info.proxyPath);
                }
                this.cert = createAll(index);
            }
            else {
                if (logger.isInfoEnabled()) {
                    logger.info("Using certificate " + info.proxyPath + " with expiration date " + this.cert.getNotAfter());
                }
            }
        }
        finally {
            fl.unlock();
        }
    }
    
    private File makeFile(String prefix, int index) {
        return new File(CA_DIR + File.separator + prefix + "." + index + ".pem");
    }

    private static final String[] ALL_NAMES = new String[] {CA_CRT_NAME_PREFIX, 
        CA_KEY_NAME_PREFIX, USER_CRT_NAME_PREFIX, USER_KEY_NAME_PREFIX, PROXY_NAME_PREFIX};
    private void deleteAll(int index) {
        for (String prefix : ALL_NAMES) {
            makeFile(prefix, index).delete();
        }
        new File(CA_DIR + File.separator + CA_CRT_NAME_PREFIX + "." + index + ".signing_policy").delete();
    }

    private int getIndex(File c) {
        String name = c.getName();
        int i2 = name.lastIndexOf('.');
        return Integer.parseInt(name.substring(i2 - 1, i2));
    }

    private int discoverNextIndex() throws GeneralSecurityException {
        for (int i = 0; i < 10; i++) {
            File f = makeFile(PROXY_NAME_PREFIX, i);
            if (!f.exists()) {
                return i;
            }
        }
        throw new GeneralSecurityException("No slots found to save CA certificate");
    }

    private File[] discoverProxies() {
        return new File(CA_DIR).listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isFile() && f.getName().matches(PROXY_NAME_PREFIX + "\\.[0-9]\\.pem");
            }
        });
    }

    private X509Certificate createAll(int index) throws GeneralSecurityException, IOException {
        logger.info("Generating CA key pair");
        KeyPair ca = CertUtil.generateKeyPair(CA_CERT_ALGORITHM, CA_CERT_BITS);
        OpenSSLKey caKey = new BouncyCastleOpenSSLKey(ca.getPrivate());
        logger.info("Self-signing CA certificate");
        X509Certificate caCert = genCert(ca.getPrivate(), ca.getPublic(), CA_CERT_DN, CA_CERT_DN, null);
        
        logger.info("Generating user key pair");
        KeyPair user = CertUtil.generateKeyPair(CA_CERT_ALGORITHM, CA_CERT_BITS);
        OpenSSLKey userKey = new BouncyCastleOpenSSLKey(user.getPrivate());
        logger.info("Signing user certificate");
        X509Certificate userCert = genCert(ca.getPrivate(), user.getPublic(), USER_CERT_DN, CA_CERT_DN,
            createExtensions(ca.getPublic(), user.getPublic()));
        logger.info("Generating proxy certificate");
        GlobusCredential proxy = makeProxy(user, userCert);
        
        try {
            logger.info("Writing keys, certificates, and proxy");
            writeKey(caKey, makeFile(CA_KEY_NAME_PREFIX, index));
            writeCert(caCert, makeFile(CA_CRT_NAME_PREFIX, index));
            writeKey(userKey, makeFile(USER_KEY_NAME_PREFIX, index));
            writeCert(userCert, makeFile(USER_CRT_NAME_PREFIX, index));
            writeProxy(proxy, makeFile(PROXY_NAME_PREFIX, index));
            copySigningPolicy(index);
        }
        catch (GeneralSecurityException e) {
            deleteAll(index);
            throw e;
        }
        return cert;
    }

    private void copySigningPolicy(int index) throws IOException {
        FileOutputStream fos = new FileOutputStream(CA_DIR + File.separator + CA_CRT_NAME_PREFIX + "." + index + ".signing_policy");
        try {
            InputStream is = AutoCA.class.getClassLoader().getResource(SIGNING_POLICY_RES_NAME).openStream();
            try {
                byte[] buf = new byte[1024];
                int read = is.read(buf);
                while (read != -1) {
                    fos.write(buf, 0, read);
                    read = is.read(buf);
                }
            }
            finally {
                is.close();
            }
        }
        finally {
            fos.close();
        }
    }

    private Map<DERObjectIdentifier, DEREncodable> createExtensions(PublicKey caPub, PublicKey userPub) throws IOException {
        Map<DERObjectIdentifier, DEREncodable> ext = new HashMap<DERObjectIdentifier, DEREncodable>();
        
        // not a CA
        ext.put(X509Extensions.BasicConstraints, new BasicConstraints(false));
        // obvious
        ext.put(X509Extensions.KeyUsage, new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.digitalSignature));
        ext.put(X509Extensions.SubjectKeyIdentifier, getSubjectKeyInfo(userPub));
        ext.put(X509Extensions.AuthorityKeyIdentifier, getAuthorityKeyIdentifier(caPub));
        
        return ext;
    }
    
    

    private DEREncodable getAuthorityKeyIdentifier(PublicKey caPub) throws IOException {
        DERObject derKey = new ASN1InputStream(caPub.getEncoded()).readObject();
        return new AuthorityKeyIdentifier(new SubjectPublicKeyInfo((ASN1Sequence) derKey));
    }

    private DEREncodable getSubjectKeyInfo(PublicKey userPub) throws IOException {
        // convert key to bouncy castle format and get subject key identifier
        DERObject derKey = new ASN1InputStream(userPub.getEncoded()).readObject();
        return new SubjectKeyIdentifier(new SubjectPublicKeyInfo((ASN1Sequence) derKey));
    }

    private void signCert(X509Certificate userCert, OpenSSLKey caKey, X509Certificate caCert) {
        gen.reset();
    }

    private GlobusCredential makeProxy(KeyPair kp, X509Certificate issuerCert) throws GeneralSecurityException {
        BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();
        KeyPair newKeyPair = CertUtil.generateKeyPair(CA_CERT_ALGORITHM, CA_CERT_BITS);
        
        return factory.createCredential(new X509Certificate[] { issuerCert },
                kp.getPrivate(), CA_CERT_BITS, (int) (CA_CERT_LIFETIME / 1000), GSIConstants.DELEGATION_FULL,
                (X509ExtensionSet) null);
    }
    
    private void writeProxy(GlobusCredential proxy, File f) throws GeneralSecurityException {
        try {
            OutputStream fw = openStream(f);
            try {
                proxy.save(fw);
            }
            finally {
                fw.close();
            }
        }
        catch (Exception e) {
            throw new GeneralSecurityException("Failed to save proxy certificate", e);
        }
    }
    
    private OutputStream openStream(File f) throws SecurityException, IOException {
        String path = f.getAbsolutePath();
        File file = Util.createFile(path);
        // set read only permissions
        if (!Util.setOwnerAccessOnly(path)) {
            logger.warn("Failed to set permissions on " + path);
        }
        return new FileOutputStream(file);
    }

    private void writeCert(X509Certificate cert, File f) throws GeneralSecurityException {
        try {
            OutputStream fw = openStream(f);
            CertUtil.writeCertificate(fw, cert);
        }
        catch (Exception e) {
            throw new GeneralSecurityException("Failed to save X509 certificate", e);
        }
    }

    private X509Certificate genCert(PrivateKey signKey, PublicKey pubKey, String subjectDN, String issuerDN, 
            Map<DERObjectIdentifier, DEREncodable> ext) throws GeneralSecurityException {
        gen.reset();
        Date now = new Date();
        
        gen.setSerialNumber(BigInteger.valueOf(0));
        gen.setNotBefore(now);
        gen.setNotAfter(new Date(now.getTime() + CA_CERT_LIFETIME));
        gen.setIssuerDN(new X509Name(issuerDN));
        gen.setSubjectDN(new X509Name(subjectDN));
        gen.setPublicKey(pubKey);
        gen.setSignatureAlgorithm(CA_CERT_SIGNATURE_ALGORITHM);
        
        if (ext != null) {
            for (Map.Entry<DERObjectIdentifier, DEREncodable> e : ext.entrySet()) {
                gen.addExtension(e.getKey(), false, e.getValue());
            }
        }
        
        try {
            X509Certificate cert = gen.generateX509Certificate(signKey, "BC", new SecureRandom());
            return cert;
        }
        catch (Exception e) {
            throw new GeneralSecurityException("Failed to create X509 certificate", e);
        }
    }

    private void writeKey(OpenSSLKey key, File f) throws GeneralSecurityException {
        try {
            OutputStream keyStream = openStream(f);
            try {
                key.writeTo(keyStream);
            }
            finally {
                keyStream.close();
            }
        }
        catch (Exception e) {
            throw new GeneralSecurityException("Failed to save CA private key", e);
        }
    }
}
