//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 8, 2012
 */
package org.globus.cog.abstraction.impl.sshcl.execution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder;
import org.globus.cog.abstraction.impl.ssh.ProxyForwardingManager;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;


public class JobSubmissionTaskHandler extends org.globus.cog.abstraction.impl.execution.local.JobSubmissionTaskHandler {
    public static final Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);
    
    private static Properties props;
    

    @Override
    protected Process startProcess(JobSpecification spec, File dir) throws IOException {
        if (spec.getDelegation() != Delegation.NO_DELEGATION) {
            try {
                ProxyForwarder.Info info = ProxyForwardingManager.getDefault().forwardProxy(spec.getDelegation(), 
                    new SSHCLProxyForwarder(getTask().getService(0), getProperties()));
                if (info != null) {
                    spec.addEnvironmentVariable("X509_USER_PROXY", info.proxyFile);
                    spec.addEnvironmentVariable("X509_CERT_DIR", info.caCertFile);
                }
            }
            catch (InvalidSecurityContextException e) {
                throw new IOException(e);
            }
        }
        return super.startProcess(spec, dir);
    }

    private synchronized static Properties getProperties() {
        if (props == null) {
            props = new Properties();
            try {
                ClassLoader cl = JobSubmissionTaskHandler.class.getClassLoader();
                InputStream is = cl.getResourceAsStream("provider-sshcl.properties");
                if (is != null) {
                    props.load(is);
                }
            }
            catch (Exception e) {
                logger.warn("Failed to load properties", e);
            }
        }
        return props;
    }
    
    protected List<String> buildCmdArray(JobSpecification spec) {
        Service service = getTask().getService(0);
        
        String ssh = getProperties().getProperty("ssh", "ssh");
        
        List<String> cmdarray = new ArrayList<String>();
        cmdarray.add(ssh);
        
        if (service.getServiceContact().getPort() > 0) {
            cmdarray.add("-p");
            cmdarray.add(String.valueOf(service.getServiceContact().getPort()));
        }

        cmdarray.add(service.getServiceContact().getHost());
        cmdarray.add("/bin/bash");
        
        /*
         * read commands from stdin.
         */
        cmdarray.add("-s");
        
        if (logger.isInfoEnabled()) {
            logger.info("SSH-CL cmd-array: " + new ArrayList<String>(cmdarray));
        }
        return cmdarray;
    }
    
    @Override
    protected void addEnvs(ProcessBuilder pb, JobSpecification spec) {
        // override to do nothing. Environment variables are passed
        // through the ssh shell
    }



    @Override
    protected void processIN(String in, File dir, OutputStream os) throws IOException {
        JobSpecification spec = (JobSpecification) getTask().getSpecification();
        
        PrintStream ps = new PrintStream(os);
        
        if (spec.getDirectory() != null) {
            ps.print("cd ");
            ps.println(escape(spec.getDirectory()));
        }
        
        for (String env : spec.getEnvironmentVariableNames()) {
            ps.print("export ");
            ps.print(escape(env));
            ps.print("=");
            ps.println(escape(spec.getEnvironmentVariable(env)));
        }
        
        /**
         * Fix for bug #1197 which is really just some messed up logic
         * in a particular piece of software that does what it shouldn't do
         * in a profile script.
         * 
         * This fix is in the wrong place, in the sense that, if anything,
         * it should be in the coaster code. Unfortunately there's no
         * nice place to fit it in the coaster code.
         */
        ps.println("export SHLVL=1");
        
        ps.print(escape(spec.getExecutable()));
        for (String arg : spec.getArgumentsAsList()) {
            ps.print(" ");
            ps.print(escape(arg));
        }
        ps.println();
        
        ps.flush();
        super.processIN(in, dir, os);
    }
        
    /*
     * Escape everything that bash cares about (I hope this is everything)
     */
    private String escape(String s) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch(c) {
                case '\\':
                case '\n':
                case '\t':
                case '"':
                case '\'':
                case '\b':
                case '\r':
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case ';':
                case '>':
                case '<':
                case '&':
                case '|':
                case '~':
                case ' ':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }
        
        return sb.toString();
    }

    @Override
    protected void cleanUp(JobSpecification spec, File dir) throws TaskSubmissionException {
        if (spec.getCleanUpSet() != null && !spec.getCleanUpSet().isEmpty()) {
            throw new TaskSubmissionException(this + " does not support cleaning up");
        }
    }

    @Override
    protected void stageOut(JobSpecification spec, File dir, boolean jobSucceeded)
            throws Exception {
        if (spec.getStageOut() != null && !spec.getStageOut().isEmpty()) {
            throw new TaskSubmissionException(this + " does not support staging");
        }
    }

    @Override
    protected void stageIn(JobSpecification spec, File dir) throws Exception {
        if (spec.getStageIn() != null && !spec.getStageIn().isEmpty()) {
            throw new TaskSubmissionException(this + " does not support staging");
        }
    }

    @Override
    public String toString() {
        return "command line SSH handler";
    }
}
