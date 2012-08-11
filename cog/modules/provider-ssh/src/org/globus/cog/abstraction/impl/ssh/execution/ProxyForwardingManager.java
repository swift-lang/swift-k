//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 23, 2010
 */
package org.globus.cog.abstraction.impl.ssh.execution;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.ssh.ConnectionID;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.SigningPolicy;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;

import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

/**
 * 
 * The non-use of the term "delegation" is intentional since there is one aspect
 * of delegation that does not apply here. When doing delegation the client
 * never sees the private key of the delegated credential.
 * 
 */
public class ProxyForwardingManager {
    public static final Logger logger = Logger.getLogger(ProxyForwardingManager.class);
    
    public static final long TIME_MARGIN = 10000;
    public static final String PROXY_PREFIX = "sshproxy";
    public static final String CA_PREFIX = "sshCAcert";

    private static ProxyForwardingManager dm;

    public static synchronized ProxyForwardingManager getDefault() {
        if (dm == null) {
            dm = new ProxyForwardingManager();
        }
        return dm;
    }

    private static class Key {
        public final ConnectionID connectionId;
        public final int delegationType;

        public Key(ConnectionID connectionId, int delegationType) {
            this.connectionId = connectionId;
            this.delegationType = delegationType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key k = (Key) obj;
                return k.connectionId.equals(connectionId)
                        && k.delegationType == delegationType;
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return connectionId.hashCode() + delegationType;
        }
    }

    public static class Info {
        public final String proxyFile, caCertFile;
        public final long expirationTime;

        public Info(String proxyFile, String caCertFile, long expirationTime) {
            this.proxyFile = proxyFile;
            this.caCertFile = caCertFile;
            this.expirationTime = expirationTime;
        }
    }

    private Map<Key, Info> state;

    private ProxyForwardingManager() {
        this.state = new HashMap<Key, Info>();
    }

    public synchronized Info forwardProxy(int type, SSHChannel s)
            throws InvalidSecurityContextException {
        if (type == Delegation.NO_DELEGATION) {
            return null;
        }
        Key key = new Key(s.getBundle().getId(), type);
        Info info = state.get(key);
        if (info == null || info.expirationTime - TIME_MARGIN < System.currentTimeMillis()) {
            info = actualForward(key, s);
            state.put(key, info);
        }
        return info;
    }

    private Info actualForward(Key key, SSHChannel s)
            throws InvalidSecurityContextException {
        try {
            GlobusCredential cred = generateNewCredential(key);
            return writeCredential(cred, s);
        }
        catch (InvalidSecurityContextException e) {
            throw e;
        }
        catch (Exception e) {
            throw new InvalidSecurityContextException(e);
        }
    }

    private Info writeCredential(GlobusCredential cred, SSHChannel s)
            throws InvalidSecurityContextException {
        try {
            SSHChannel cp = s.getBundle().allocateChannel();
            SftpSubsystemClient sftp = new SftpSubsystemClient();
            try {
                if (!cp.getSession().startSubsystem(sftp)) {
                    throw new InvalidSecurityContextException(
                        "Failed to start the SFTP subsystem on " + cp.getBundle().getId());
                }

                String globusDir = makeGlobusDir(sftp);
                cleanupOldProxies(sftp, globusDir);
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
                long now = System.currentTimeMillis();
                int id = Math.abs(random.nextInt());
                long suffix = cred.getTimeLeft() + now / 1000;
                String proxyFileName = PROXY_PREFIX + "-" + id + "-" + suffix;
                String caCertFileName = CA_PREFIX + "-" + id + "-" + suffix + ".pem";
                String signingPolicyFileName = CA_PREFIX + "-" + id + "-" + suffix + ".signing_policy";
                
                
                
                SftpFile fp = createFile(sftp, globusDir, proxyFileName);
                BufferedOutputStream pout = new BufferedOutputStream(new SftpFileOutputStream(fp));
                cred.save(pout);
                pout.close();
                
                X509Certificate[] chain = cred.getCertificateChain();
                X509Certificate userCert = chain[chain.length - 1];
                TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
                
                X509Certificate caCert = tc.getCertificate(userCert.getIssuerDN().getName());
                if (caCert == null) {
                    logger.warn("Cannot find root CA certificate for proxy");
                    logger.warn("DNs of trusted certificates:");
                    X509Certificate[] roots = tc.getCertificates();
                    for (X509Certificate root : roots) {
                        logger.warn("\t" + root.getSubjectDN());
                    }
                    throw new InvalidSecurityContextException("Failed to find root CA certificate (" + userCert.getIssuerDN().getName() + ")");
                }
                
                
                SftpFile fc = createFile(sftp, globusDir, caCertFileName);
                BufferedOutputStream cout = new BufferedOutputStream(new SftpFileOutputStream(fc));
                CertUtil.writeCertificate(cout, caCert);
                cout.close();
                
                SigningPolicy sp = tc.getSigningPolicy('/' + userCert.getIssuerDN().getName().replace(',', '/'));
                if (sp != null) {
                    SftpFile spf = createFile(sftp, globusDir, signingPolicyFileName);
                    BufferedOutputStream spout = new BufferedOutputStream(new SftpFileOutputStream(spf));
                    writeFile(spout, new FileInputStream(sp.getFileName()));
                }
                
                
                return new Info(globusDir + "/" + proxyFileName, globusDir + "/" + caCertFileName, cred.getTimeLeft() * 1000
                        + System.currentTimeMillis());
            }
            finally {
                sftp.stop();
                s.getBundle().releaseChannel(cp);
            }
        }
        catch (InvalidSecurityContextException e) {
            throw e;
        }
        catch (Exception e) {
            throw new InvalidSecurityContextException(e);
        }
    }
    
    private void writeFile(OutputStream out, InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        while (len != -1) {
            out.write(buf, 0, len);
            len = in.read(buf);
        }
        out.close();
        in.close();
    }

    private SftpFile createFile(SftpSubsystemClient sftp, String dir, String name) throws IOException {
        FileAttributes fa = new FileAttributes();
        fa.setPermissions(new UnsignedInteger32(FileAttributes.S_IRUSR
                | FileAttributes.S_IWUSR));
        SftpFile f = sftp.openFile(dir + "/" + name,
                SftpSubsystemClient.OPEN_WRITE
                        | SftpSubsystemClient.OPEN_CREATE
                        | SftpSubsystemClient.OPEN_EXCLUSIVE,
                fa);
        // specifying the permissions to sftp.openFile() above doesn't
        // seem to work
        sftp.changePermissions(f, fa.getPermissions().intValue());
        return f;
    }

    private static final Pattern PROXY_NAME_PATTERN = Pattern
            .compile("sshproxy-([0-9]+)-([0-9]+)");

    private void cleanupOldProxies(SftpSubsystemClient sftp, String globusDir)
            throws IOException {
        List<SftpFile> files = new ArrayList<SftpFile>();
        SftpFile dir = sftp.openDirectory(globusDir);
        try {
            sftp.listChildren(dir, files);
            long nowSecs = System.currentTimeMillis() / 1000;
            for (SftpFile f : files) {
                Matcher m = PROXY_NAME_PATTERN.matcher(f.getFilename());
                if (m.matches()) {
                    long expirationTime = Long.parseLong(m.group(2));
                    if (expirationTime < nowSecs) {
                        sftp.removeFile(globusDir + "/" + f.getFilename());
                    }
                }
            }
        }
        finally {
            dir.close();
        }
    }

    private String makeGlobusDir(SftpSubsystemClient sftp) throws IOException {
        String home = sftp.getDefaultDirectory();
        String globusDir = home + "/.globus";
        try {
            sftp.getAttributes(globusDir);
        }
        catch (Exception e) {
            sftp.makeDirectory(globusDir);
        }
        return globusDir;
    }

    private GlobusCredential generateNewCredential(Key key) throws GlobusCredentialException,
            InvalidSecurityContextException, GeneralSecurityException {
        GlobusCredential src = GlobusCredential.getDefaultCredential();
        if (src == null) {
            throw new InvalidSecurityContextException("No default credential found");
        }

        // If only the security stuff in DelegationUtil@GT4 would be
        // separable from the WS crap
        BouncyCastleCertProcessingFactory factory =
                    BouncyCastleCertProcessingFactory.getDefault();
        int delegType = (key.delegationType == Delegation.FULL_DELEGATION ?
                    GSIConstants.DELEGATION_FULL : GSIConstants.DELEGATION_LIMITED);

        KeyPair newKeyPair = CertUtil.generateKeyPair("RSA", src.getStrength());

        X509Certificate[] srcChain = src.getCertificateChain();
        X509Certificate newCert = null;

        try {
            newCert = factory.createProxyCertificate(srcChain[0],
                                        src.getPrivateKey(),
                                        newKeyPair.getPublic(), -1,
                                        key.delegationType == Delegation.FULL_DELEGATION ?
                                                GSIConstants.DELEGATION_FULL
                                                : GSIConstants.DELEGATION_LIMITED,
                                        (X509ExtensionSet) null, null);
        }
        catch (GeneralSecurityException e) {
            throw new InvalidSecurityContextException("Delegation failed", e);
        }

        X509Certificate[] newChain = new X509Certificate[srcChain.length + 1];
        newChain[0] = newCert;
        System.arraycopy(srcChain, 0, newChain, 1, srcChain.length);

        return new GlobusCredential(newKeyPair.getPrivate(), newChain);
    }
}
