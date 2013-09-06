//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 18, 2012
 */
package org.globus.cog.abstraction.impl.sshcl.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.ssh.ConnectionID;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.SigningPolicy;
import org.globus.util.Util;

public class SSHCLProxyForwarder extends ProxyForwarder {
    public static final Logger logger = Logger.getLogger(SSHCLProxyForwarder.class);
    
    public static final int DELETE_BULK_SIZE = 10;
    
    private String host;
    private int port;
    private Properties properties;
    private ConnectionID cid;
    private TaskHandler th;

    public SSHCLProxyForwarder(Service service, Properties properties) {
        this.properties = properties;
        this.host = service.getServiceContact().getHost();
        this.port = service.getServiceContact().getPort();
        if (this.port <= 0) {
            this.port = 22; 
        }
        cid = new ConnectionID(this.host, this.port, null);
    }

    @Override
    public Key getKey(int type) {
        return new Key(cid, type);
    }
    
    private static class MFileOutputStream extends FileOutputStream {
        private File f;
        
        public MFileOutputStream(File f) throws FileNotFoundException {
            super(f);
            this.f = f;
        }
        
        public File getFile() {
            return f;
        }
    }

    public Info writeCredential(GlobusCredential cred)
            throws InvalidSecurityContextException {
        try {
            String globusDir = makeGlobusDir();
            cleanupOldProxies(globusDir);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            long now = System.currentTimeMillis();
            int id = Math.abs(random.nextInt());
            long suffix = cred.getTimeLeft() + now / 1000;
            String proxyFileName = PROXY_PREFIX + "-" + id + "-" + suffix;
            String caCertFileName = CA_PREFIX + "-" + id + "-" + suffix + ".pem";
            String signingPolicyFileName = CA_PREFIX + "-" + id + "-" + suffix + ".signing_policy";
            
            logger.info("Copying proxy");
            MFileOutputStream pout = makeTmpFile(proxyFileName); 
            cred.save(pout);
            pout.close();
            moveFile(pout.getFile(), globusDir, proxyFileName);
            
            X509Certificate[] chain = cred.getCertificateChain();
            X509Certificate userCert = chain[chain.length - 1];                
            X509Certificate caCert = getCaCert(userCert);
            
            logger.info("Copying certificate");
            MFileOutputStream cout = makeTmpFile(caCertFileName);
            CertUtil.writeCertificate(cout, caCert);
            cout.close();
            moveFile(cout.getFile(), globusDir, caCertFileName);
                
            logger.info("Copying signing policy file");
            SigningPolicy sp = getSigningPolicy(userCert);
            if (sp != null) {
                MFileOutputStream spout = makeTmpFile(signingPolicyFileName);
                streamCopy(spout, new FileInputStream(sp.getFileName()));
                moveFile(spout.getFile(), globusDir, signingPolicyFileName);
            }
                
                
            return new Info(globusDir + "/" + proxyFileName, globusDir + "/" + caCertFileName, cred.getTimeLeft() * 1000
                    + System.currentTimeMillis());
        }
        catch (InvalidSecurityContextException e) {
            throw e;
        }
        catch (Exception e) {
            throw new InvalidSecurityContextException(e);
        }
    }

    private void moveFile(File src, String destDir, String dest) throws IOException {
        runSCP(src, destDir + "/" + dest);
    }

    private MFileOutputStream makeTmpFile(String name) throws SecurityException, IOException {
        String path = System.getProperty("java.io.tmpdir") + File.separator + name;
        File file = Util.createFile(path);
        // set read only permissions
        if (!Util.setOwnerAccessOnly(path)) {
            logger.warn("Failed to set permissions on " + path);
        }
        return new MFileOutputStream(file);
    }

    private static final Pattern PROXY_NAME_PATTERN = Pattern
            .compile("ssh(proxy|CAcert)-([0-9]+)-([0-9]+).*");

    private void cleanupOldProxies(String globusDir)
            throws IOException {
        try {
            logger.info("Cleaning up old proxies");
            String output = runSSH(new String[] {"ls", "-1", globusDir + "/ssh*-*-*"});
            String[] files = output.split("\\n");
            List<String> deleteQueue = new ArrayList<String>();
            long nowSecs = System.currentTimeMillis() / 1000;
            for (String f : files) {
                f = f.substring(f.lastIndexOf('/') + 1);
                Matcher m = PROXY_NAME_PATTERN.matcher(f);
                if (m.matches()) {
                    long expirationTime = Long.parseLong(m.group(3));
                    if (expirationTime < nowSecs) {
                        deleteQueue.add(globusDir + "/" + f);
                        if (deleteQueue.size() == DELETE_BULK_SIZE) {
                            deleteFiles(deleteQueue);
                        }
                    }
                }
            }
            // delete remaining files
            deleteFiles(deleteQueue);
            
        }
        catch (IOException e) {
            if (e.getMessage() == null 
            		|| (!e.getMessage().contains("No such file or directory") 
            				&& !e.getMessage().contains("No match"))) {
                throw e;
            }
            else {
                // nothing to clean
            }
        }
    }

    private void deleteFiles(List<String> l) throws IOException {
        if (l.isEmpty()) {
            return;
        }
        
        logger.info("Removing " + l);
        String[] cmdline = new String[l.size() + 2];
        cmdline[0] = "rm";
        cmdline[1] = "-f";
        for (int i = 0; i < l.size(); i++) {
            cmdline[i + 2] = l.get(i);
        }
        runSSH(cmdline);
    }

    private String makeGlobusDir() throws IOException {
        String h = runSSH(new String[] {"mkdir", "-p", "~/.globus", ";", "ls", "-d", "~"});
        return h.trim() + "/.globus";
    }

    private String runSSH(String[] args) throws IOException {
        List<String> l = new ArrayList<String>();
        l.add("-p");
        l.add(String.valueOf(port));
        l.add(host);
        l.addAll(Arrays.asList(args));
        return runProcess("ssh", l);
    }
    
    private void runSCP(File src, String dest) throws IOException {
        List<String> l = new ArrayList<String>();
        l.add("-p"); // preserve permissions
        l.add("-P");
        l.add(String.valueOf(port));
        l.add(src.getAbsolutePath());
        l.add(host + ":" + dest);
        runProcess("scp", l);
    }

    private String runProcess(String exec, List<String> args) throws IOException {
        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        exec = properties.getProperty(exec, exec);
        JobSpecification spec = new JobSpecificationImpl();
        if (logger.isDebugEnabled()) {
            logger.debug("Running " + exec + " with args " + args);
        }
        spec.setExecutable(exec);
        spec.setArguments(args);
        spec.setStdOutputLocation(FileLocation.MEMORY);
        spec.setStdErrorLocation(FileLocation.MEMORY);
        t.setSpecification(spec);
        try {
            synchronized(this) {
                if (th == null) {
                    th = AbstractionFactory.newExecutionTaskHandler("local");
                }
            }
            th.submit(t);
            t.waitFor();
            if (t.getStatus().getStatusCode() == Status.FAILED) {
                throw new IOException("Failed to run " + exec + ": " + t.getStdError(), 
                        t.getStatus().getException());
            }
            return t.getStdOutput();
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }
}
