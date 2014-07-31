/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh.execution;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder;
import org.globus.cog.abstraction.impl.ssh.ProxyForwardingManager;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.impl.ssh.SSHChannelManager;
import org.globus.cog.abstraction.impl.ssh.SSHRunner;
import org.globus.cog.abstraction.impl.ssh.SSHSecurityContextImpl;
import org.globus.cog.abstraction.impl.ssh.SSHTaskStatusListener;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements 
        SSHTaskStatusListener {
    public static final Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());
    
    private Exec exec;
    private SSHChannel s;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        checkAndSetTask(task);
        JobSpecification spec;
        try {
            spec = (JobSpecification) task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }
        // prepare command
        String cmd = prepareSpecification(spec);
        String host = task.getService(0).getServiceContact().getHost();
        int port = task.getService(0).getServiceContact().getPort();

        s = SSHChannelManager.getDefault().getChannel(host,
                port, getSecurityContext().getCredentials());
         
        
        exec = new Exec();
        if (spec.getDelegation() != Delegation.NO_DELEGATION) {
            ProxyForwarder.Info info = ProxyForwardingManager.getDefault().forwardProxy(spec.getDelegation(), 
                new SSHProxyForwarder(s));
            if (info != null) {
                exec.addEnv("X509_USER_PROXY", info.proxyFile);
                exec.addEnv("X509_CERT_DIR", info.caCertFile);
            }
        }
        exec.setCmd(cmd);
        exec.setDir(spec.getDirectory());
        
        if (FileLocation.LOCAL.overlaps(spec.getStdInputLocation())) {
            throw new IllegalSpecException(
                    "The SSH provider does not support local input");
        }

        if (FileLocation.LOCAL.overlaps(spec.getStdOutputLocation())
                && notEmpty(spec.getStdOutput())) {
            exec.setOutFile(spec.getStdOutput());
        }
        if (FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
            exec.setOutMem(true);
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdOutputLocation())) {
            exec.setRemoteOut(spec.getStdOutput());
        }

        if (FileLocation.LOCAL.overlaps(spec.getStdErrorLocation())
                && notEmpty(spec.getStdError())) {
            exec.setErrFile(spec.getStdError());
        }
        if (FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
            exec.setErrMem(true);
        }
        if (FileLocation.REMOTE.overlaps(spec.getStdErrorLocation())) {
            exec.setRemoteErr(spec.getStdError());
        }
        
        if (FileLocation.REMOTE.overlaps(spec.getStdInputLocation())) {
            exec.setRemoteIn(spec.getStdInput());
        }

        SSHRunner r = new SSHRunner(s, exec);
        r.addListener(this);
        synchronized(this) {
            if (task.getStatus().getStatusCode() != Status.CANCELED) {
                r.startRun(exec);
                task.setStatus(Status.ACTIVE);
            }
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public synchronized void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        //TODO implement me
    }

    private String prepareSpecification(JobSpecification spec)
            throws TaskSubmissionException, IllegalSpecException {
        StringBuffer cmd = new StringBuffer();
        append(cmd, spec.getExecutable());
        if (spec.getArgumentsAsString() != null) {
            cmd.append(' ');
            Iterator<String> i = spec.getArgumentsAsList().iterator();
            while (i.hasNext()) {
                String arg = i.next();
                append(cmd, arg);
                if (i.hasNext()) {
                    cmd.append(' ');
                }
            }
        }
        return cmd.toString();
    }

    private boolean notEmpty(String str) {
        return str != null && !str.equals("");
    }
    
    private static boolean[] ESCAPE = new boolean[256];
    
    static {
        ESCAPE['\''] = true;
        ESCAPE['\\'] = true;
        ESCAPE[' '] = true;
        ESCAPE['>'] = true;
        ESCAPE['<'] = true;
        ESCAPE['&'] = true;
        ESCAPE['|'] = true;
        ESCAPE['('] = true;
        ESCAPE[')'] = true;
        ESCAPE['~'] = true;
        ESCAPE['#'] = true;
        ESCAPE['$'] = true;
        ESCAPE['*'] = true;
        ESCAPE['`'] = true;
        ESCAPE['"'] = true;
        ESCAPE[';'] = true;
    }

    private void append(StringBuffer sb, String str) {
        if (str.length() == 0) {
            sb.append("\"\"");
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 256 && c > 0 && ESCAPE[c]) {
                sb.append('\\');
            }
            sb.append(c);
        }
    }
    
    private void space(StringBuffer sb) {
        sb.append(' ');
    }

    private void cleanup() {
        SSHChannelManager.getDefault().releaseChannel(s);
    }

    /*
     * public void outputChanged(String s) { String output =
     * this.task.getStdOutput(); if (output != null) { output += s; } else {
     * output = s; } this.task.setStdOutput(output); }
     */

    public void outputClosed() {
    }

    public void SSHTaskStatusChanged(int status, Exception e) {
        JobSpecification spec = (JobSpecification) getTask().getSpecification();
        if (status == SSHTaskStatusListener.COMPLETED) {
            getTask().setStdOutput(exec.getTaskOutput());
            getTask().setStdError(exec.getTaskError());
            getTask().setStatus(Status.COMPLETED);
        }
        else if (status == SSHTaskStatusListener.FAILED) {
            getTask().setStdOutput(exec.getTaskOutput());
            getTask().setStdError(exec.getTaskError());
            failTask(null, e);
        }
        else {
            logger.warn("Unknown status code: " + status);
            return;
        }
        cleanup();
    }

    private SecurityContext getSecurityContext() {
        SecurityContext securityContext = getTask().getService(0)
                .getSecurityContext();
        if (securityContext == null) {
            // create default credentials
            securityContext = new SSHSecurityContextImpl();
        }
        return securityContext;
    }
}