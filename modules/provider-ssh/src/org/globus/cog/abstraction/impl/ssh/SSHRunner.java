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

package org.globus.cog.abstraction.impl.ssh;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

public class SSHRunner implements Runnable {
    private SSHTask crt;
    private SSHChannel s;
    private List listeners;
    private boolean shutdownFlag;
    private static int id;

    public SSHRunner(SSHChannel s, SSHTask task) {
        this.crt = task;
        this.s = s;
    }

    public Thread wrapInThread() {
        Thread t = new Thread(this);
        synchronized (SSHRunner.class) {
            t.setName("SSH Thread " + (id++));
        }
        t.setDaemon(true);
        return t;
    }

    public void run() {
        try {
            runTask(crt);
            notifyListeners(SSHTaskStatusListener.COMPLETED, null);
        }
        catch (Exception e) {
            notifyListeners(SSHTaskStatusListener.FAILED, e);
        }
    }

    public void runTask(SSHTask t) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException, JobException {
        t.execute(s.getSession());
    }

    public void startRun(SSHTask run) {
        this.crt = run;
        Thread t = wrapInThread();
        t.start();
    }

    public void addListener(SSHTaskStatusListener l) {
        if (listeners == null) {
            listeners = new LinkedList();
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(SSHTaskStatusListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void notifyListeners(int event, Exception e) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((SSHTaskStatusListener) i.next()).SSHTaskStatusChanged(event, e);
        }
    }
}
