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

import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;


public class JobSubmissionTaskHandler extends org.globus.cog.abstraction.impl.execution.local.JobSubmissionTaskHandler {
    
    protected String[] buildCmdArray(JobSpecification spec) {
        Service service = getTask().getService(0);
        
        List<String> cmdarray = new ArrayList<String>();
        cmdarray.add("ssh");
        //cmdarray.add("/home/mike/soft/bin/tssh");
        //cmdarray.add("-v");
        //cmdarray.add("-v");
        //cmdarray.add("-v");
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
        
        return cmdarray.toArray(new String[0]);
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
