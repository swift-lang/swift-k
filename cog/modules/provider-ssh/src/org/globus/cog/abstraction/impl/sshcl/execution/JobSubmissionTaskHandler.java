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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;


public class JobSubmissionTaskHandler extends org.globus.cog.abstraction.impl.execution.local.JobSubmissionTaskHandler {
    public static final Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);
    
    private static Properties props;
    
    private synchronized static Properties getProperties() {
        if (props == null) {
            props = new Properties();
            try {
                ClassLoader cl = JobSubmissionTaskHandler.class.getClassLoader(); 
                props.load(cl.getResourceAsStream("provider-sshcl.properties")); 
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

        if (spec.getAttribute("username") != null) {
            cmdarray.add("-l");
            cmdarray.add(spec.getAttribute("username").toString());
        }
        
        if (spec.getAttribute("key") != null) {
            cmdarray.add("-i");
            cmdarray.add(spec.getAttribute("key").toString());
        }
        
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
            ps.print(escape(env));
            ps.print("=");
            ps.println(escape(spec.getEnvironmentVariable(env)));
        }
        
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
