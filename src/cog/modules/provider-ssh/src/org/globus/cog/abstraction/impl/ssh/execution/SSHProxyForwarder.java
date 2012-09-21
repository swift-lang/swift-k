//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 18, 2012
 */
package org.globus.cog.abstraction.impl.ssh.execution;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.SigningPolicy;

import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

public class SSHProxyForwarder extends ProxyForwarder {
    public static final Logger logger = Logger.getLogger(SSHProxyForwarder.class);
    
    private SSHChannel s;
    
    public SSHProxyForwarder(SSHChannel s) {
        this.s = s;
    }

    @Override
    public Key getKey(int type) {
        return new Key(s.getBundle().getId(), type);
    }

    public Info writeCredential(GlobusCredential cred)
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
                X509Certificate caCert = getCaCert(userCert);
                
                SftpFile fc = createFile(sftp, globusDir, caCertFileName);
                BufferedOutputStream cout = new BufferedOutputStream(new SftpFileOutputStream(fc));
                CertUtil.writeCertificate(cout, caCert);
                cout.close();
                
                SigningPolicy sp = getSigningPolicy(userCert);
                if (sp != null) {
                    SftpFile spf = createFile(sftp, globusDir, signingPolicyFileName);
                    BufferedOutputStream spout = new BufferedOutputStream(new SftpFileOutputStream(spf));
                    streamCopy(spout, new FileInputStream(sp.getFileName()));
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
            .compile("ssh(proxy|CAcert)-([0-9]+)-([0-9]+).*");

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
                    long expirationTime = Long.parseLong(m.group(3));
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
}
