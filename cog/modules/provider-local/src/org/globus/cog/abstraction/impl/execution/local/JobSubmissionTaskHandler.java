//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.local;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.util.NullOutputStream;
import org.globus.cog.abstraction.impl.common.util.OutputStreamMultiplexer;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * @author Kaizar Amin (amin@mcs.anl.gov)
 * 
 */
public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        Runnable {
    private static Logger logger = Logger
            .getLogger(JobSubmissionTaskHandler.class);

    private static final int STDOUT = 0;
    private static final int STDERR = 1;

    public static final int BUFFER_SIZE = 1024;

    private Task task = null;
    private Thread thread = null;
    private Process process;
    private volatile boolean killed;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
        }
        else if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
            throw new TaskSubmissionException(
                    "Task is not in unsubmitted state");
        }
        else {
            this.task = task;
            task.setStatus(Status.SUBMITTING);
            JobSpecification spec;
            try {
                spec = (JobSpecification) this.task.getSpecification();
            }
            catch (Exception e) {
                throw new IllegalSpecException(
                        "Exception while retrieving Job Specification", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(spec.toString());
            }

            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Submitting task " + task);
                }
                synchronized (this) {
                    thread = new Thread(this);
                    thread.setName("Local task " + task.getIdentity());
                    thread.setDaemon(true);
                    if (this.task.getStatus().getStatusCode() != Status.CANCELED) {
                        this.task.setStatus(Status.SUBMITTED);
                        this.thread.start();
                        if (spec.isBatchJob()) {
                            this.task.setStatus(Status.COMPLETED);
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new TaskSubmissionException("Cannot submit job", e);
            }
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this) {
            killed = true;
            process.destroy();
            this.task.setStatus(Status.CANCELED);
        }
    }

    private static final FileLocation REDIRECT_LOCATION = FileLocation.MEMORY
            .and(FileLocation.LOCAL);

    public void run() {
        try {
            // TODO move away from the multi-threaded approach
            JobSpecification spec = (JobSpecification) this.task
                    .getSpecification();

            File dir = null;
            if (spec.getDirectory() != null) {
                dir = new File(spec.getDirectory());
            }

            process = Runtime.getRuntime().exec(buildCmdArray(spec),
                    buildEnvp(spec), dir);
            this.task.setStatus(Status.ACTIVE);

            processIN(spec.getStdInput(), dir);

            List pairs = new LinkedList();

            if (!FileLocation.NONE.equals(spec.getStdOutputLocation())) {
                OutputStream os = prepareOutStream(spec.getStdOutput(), spec
                        .getStdOutputLocation(), dir, task, STDOUT);
                if (os != null) {
                    pairs.add(new StreamPair(process.getInputStream(), os));
                }
            }

            if (!FileLocation.NONE.equals(spec.getStdErrorLocation())) {
                OutputStream os = prepareOutStream(spec.getStdError(), spec
                        .getStdErrorLocation(), dir, task, STDERR);
                if (os != null) {
                    pairs.add(new StreamPair(process.getErrorStream(), os));
                }
            }

            Processor p = new Processor(process, pairs);

            if (spec.isBatchJob()) {
                Thread t = new Thread(p);
                t.setName("Local task");
                t.start();
                return;
            }

            p.run();
            int exitCode = p.getExitCode();

            if (logger.isDebugEnabled()) {
                logger.debug("Exit code was " + exitCode);
            }
            if (killed) {
                return;
            }
            if (exitCode == 0) {
                this.task.setStatus(Status.COMPLETED);
            }
            else {
                throw new JobException(exitCode);
            }
        }
        catch (Exception e) {
            if (killed) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Exception while running local executable", e);
            }
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
        }
    }

    protected void processIN(String in, File dir) throws IOException {

        byte[] buf = new byte[BUFFER_SIZE];
        if (in != null) {
            OutputStream out = process.getOutputStream();

            File stdin;
            if (dir != null) {
                stdin = new File(dir, in);
            }
            else {
                stdin = new File(in);
            }

            FileInputStream file = new FileInputStream(stdin);
            int read = file.read(buf);
            while (read != -1) {
                out.write(buf, 0, read);
                read = file.read(buf);
            }
            file.close();
        }
    }

    protected OutputStream prepareOutStream(String out, FileLocation loc,
            File dir, Task task, int stream) throws IOException {

        OutputStream os = null;
        TaskOutputStream baos = null;
        if (FileLocation.MEMORY.overlaps(loc)) {
            baos = new TaskOutputStream(task, stream);
            os = baos;
        }
        if ((FileLocation.LOCAL.overlaps(loc) || FileLocation.REMOTE
                .equals(loc))
                && out != null) {
            if (os != null) {
                os = new OutputStreamMultiplexer(os,
                        new FileOutputStream(out));
            }
            else {
                os = new FileOutputStream(out);
            }
        }
        if (os == null) {
            os = new NullOutputStream();
        }

        return os;
    }

    private String[] buildCmdArray(JobSpecification spec) {
        List arguments = spec.getArgumentsAsList();
        String[] cmdarray = new String[arguments.size() + 1];

        cmdarray[0] = spec.getExecutable();
        Iterator i = arguments.iterator();
        int index = 1;
        while (i.hasNext()) {
            cmdarray[index++] = (String) i.next();
        }
        return cmdarray;
    }

    private String[] buildEnvp(JobSpecification spec) {
        Collection names = spec.getEnvironmentVariableNames();
        if (names.size() == 0) {
            /*
             * Questionable. An envp of null will cause the parent environment
             * to be inherited, while an empty one will cause no environment
             * variables to be set for the process. Or so it seems from the
             * Runtime.exec docs.
             */
            return null;
        }
        String[] envp = new String[names.size()];
        Iterator i = names.iterator();
        int index = 0;
        while (i.hasNext()) {
            String name = (String) i.next();
            envp[index++] = name + "=" + spec.getEnvironmentVariable(name);
        }
        return envp;
    }

    private static class StreamPair {
        public InputStream is;
        public OutputStream os;

        public StreamPair(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }
    }

    private static class TaskOutputStream extends ByteArrayOutputStream {
        private Task task;
        private int stream;

        public TaskOutputStream(Task task, int stream) {
            this.task = task;
            this.stream = stream;
        }

        public void close() throws IOException {
            super.close();
            String value = toString();
            if (logger.isDebugEnabled()) {
                logger.debug((stream == STDOUT ? "STDOUT" : "STDERR")
                        + " from job: " + value);
            }
            if (stream == STDOUT) {
                task.setStdOutput(value);
            }
            else {
                task.setStdError(value);
            }
        }
    }

    private static class Processor implements Runnable {
        private Process p;
        private List streamPairs;
        byte[] buf;

        public Processor(Process p, List streamPairs) {
            this.p = p;
            this.streamPairs = streamPairs;
        }

        public void run() {
            try {
                run2();
            }
            catch (Exception e) {
                logger.warn("Exception caught while running process", e);
            }
        }

        public void run2() throws IOException, InterruptedException {
            while (true) {
                boolean any = processPairs();
                if (processDone()) {
                    closePairs();
                }
                else {
                    if (!any) {
                        Thread.sleep(20);
                    }
                }
            }
        }

        private boolean processPairs() throws IOException {
            boolean any = false;
            Iterator i = streamPairs.iterator();
            while (i.hasNext()) {
                if (buf == null) {
                    buf = new byte[BUFFER_SIZE];
                }
                StreamPair sp = (StreamPair) i.next();
                int avail = sp.is.available();
                if (avail > 0) {
                    any = true;
                    int len = sp.is.read(buf);
                    sp.os.write(buf, 0, len);
                }
            }
            return any;
        }
       
        private boolean processDone() {
            try {
                p.exitValue();
                return true;
            }
            catch (IllegalThreadStateException e) {
                return false;
            }
        }

        private void closePairs() throws IOException {
            Iterator i = streamPairs.iterator();
            while (i.hasNext()) {
                StreamPair sp = (StreamPair) i.next();
                sp.os.close();
            }
        }
        public int getExitCode() {
            return p.exitValue();
        }
    }
}
