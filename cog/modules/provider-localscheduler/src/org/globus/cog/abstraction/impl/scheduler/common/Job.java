//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.FileLocation;

public class Job {
    public static final Logger logger = Logger.getLogger(Job.class);

    public static final int STATE_NONE = 0;
    public static final int STATE_QUEUED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_DONE = 3;
    public static final int STATE_UNKNOWN = 4;

    private String jobID;
    private String exitcodeFileName;
    private String stdout, stderr;
    private FileLocation outLoc, errLoc;
    protected ProcessListener listener;
    protected int state;
    private int ticks;

    public Job(String jobID, String stdout, FileLocation outLoc, String stderr, FileLocation errLoc,
            String exitcodeFileName, ProcessListener listener) {
        this.jobID = jobID;
        this.listener = listener;
        this.stdout = stdout;
        this.stderr = stderr;
        this.outLoc = outLoc;
        this.errLoc = errLoc;
        this.state = STATE_NONE;
        this.exitcodeFileName = exitcodeFileName;
        this.ticks = 0;
    }

    public boolean close() {
        if (!processStdout() || !processStderr()) {
            return true;
        }
        File f = new File(exitcodeFileName);
        if (!f.exists()) {
            if (ticks == 5) {
                listener
                        .processFailed(new ProcessException(
                                "Exitcode file not found 5 queue polls after the job was reported done"));
                return true;
            }
            else {
                ticks++;
                return false;
            }
        }
        else {
            processExitCode();
            return true;
        }
    }

    protected boolean processExitCode() {

        int exitcode;
        try {
            exitcode = Integer.parseInt(new BufferedReader(new FileReader(
                    exitcodeFileName)).readLine());
            listener.processCompleted(exitcode);
            return true;
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception caught while reading exit code", e);
            }
            listener.processFailed(new ProcessException(
                    "Exception caught while reading exit code", e));
            return false;
        }
    }

    protected boolean processStdout() {
        try {
            if (FileLocation.MEMORY.overlaps(outLoc)) {
                String out = readFile(stdout);
                if (out != null && !"".equals(out)) {
                    listener.stdoutUpdated(out);
                }
            }
            return true;
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception caught while reading STDOUT", e);
            }
            listener.processFailed(new ProcessException(
                    "Exception caught while reading STDOUT", e));
            return false;
        }
    }

    protected boolean processStderr() {
        try {
            if (FileLocation.MEMORY.overlaps(errLoc)) {
                String err = readFile(stderr);
                if (err != null && !"".equals(err)) {
                    listener.stderrUpdated(err);
                }
            }
            return true;
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception caught while reading STDERR", e);
            }
            listener.processFailed(new ProcessException(
                    "Exception caught while reading STDERR", e));
            return false;
        }
    }

    public synchronized void await() {
        while (state != STATE_DONE) {
            try {
                wait();
            }
            catch (InterruptedException e) {
            }
        }
    }

    public String getJobID() {
        return jobID;
    }

    public void setState(int state) {
        if (state == this.state) {
            return;
        }
        else {
            if (state == STATE_DONE) {
                if (close()) {
                    this.state = STATE_DONE;
                    synchronized (this) {
                        notify();
                    }
                }
            }
            else {
                this.state = state;
                if (listener != null) {
                    listener.statusChanged(state);
                }
            }
        }
    }

    public int getState() {
        return state;
    }

    public void fail(String message) {
        listener.processFailed(message);
    }

    protected String readFile(String name) throws IOException {
        File f = new File(name);
        if (!f.exists()) {
            return null;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line;
        do {
            line = br.readLine();
            if (line != null) {
                sb.append(line);
                sb.append('\n');
            }
        } while (line != null);
        return sb.toString();
    }
}
