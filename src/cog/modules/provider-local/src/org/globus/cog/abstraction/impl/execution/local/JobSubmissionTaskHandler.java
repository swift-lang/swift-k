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
            throw new TaskSubmissionException("Task is not in unsubmitted state");
        }
        else {
            this.task = task;
            task.setStatus(Status.SUBMITTING);
            JobSpecification spec;
            try {
                spec = (JobSpecification) this.task.getSpecification();
            } catch (Exception e) {
                throw new IllegalSpecException(
                        "Exception while retreiving Job Specification", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(spec.toString());
            }

            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Submitting task " + task);
                }
                synchronized(this) {
                    this.thread = new Thread(this);
                    if (this.task.getStatus().getStatusCode() != Status.CANCELED) {
                        this.task.setStatus(Status.SUBMITTED);
                        this.thread.start();
                        if (spec.isBatchJob()) {
                            this.task.setStatus(Status.COMPLETED);
                        }
                    }
                }
            } catch (Exception e) {
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
        synchronized(this) {
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

            // reusable byte buffer
            byte[] buf = new byte[BUFFER_SIZE];
            /*
             * This should be interleaved with stdout processing, since a
             * process could block if its STDOUT is not consumed, thus causing a
             * deadlock
             */
            processIN(spec.getStdInput(), dir, buf);

            if (!FileLocation.NONE.equals(spec.getStdOutputLocation())) {
                String out = processOUT(spec.getStdOutput(), spec
                        .getStdOutputLocation(), dir, buf, process
                        .getInputStream());
                if (out != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("STDOUT from job: " + out);
                    }
                    this.task.setStdOutput(out);
                }
            }

            if (!FileLocation.NONE.equals(spec.getStdErrorLocation())) {
                String err = processOUT(spec.getStdError(), spec
                        .getStdErrorLocation(), dir, buf, process
                        .getErrorStream());
                if (err != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("STDERR from job: " + err);
                    }
                    this.task.setStdError(err);
                }
            }

            if (spec.isBatchJob()) {
                return;
            }

            int exitCode = process.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug("Exit code was " + exitCode);
            }
            if (killed) {
                return;
            }
            if (exitCode == 0) {
                this.task.setStatus(Status.COMPLETED);
            } else {
                throw new JobException(exitCode);
            }
        } catch (Exception e) {
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

    protected void processIN(String in, File dir, byte[] buf)
            throws IOException {
        if (in != null) {
            OutputStream out = process.getOutputStream();

            File stdin;
            if (dir != null) {
                stdin = new File(dir, in);
            } else {
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

    protected String processOUT(String out, FileLocation loc, File dir,
            byte[] buf, InputStream pin) throws IOException {

        OutputStream os = null;
        ByteArrayOutputStream baos = null;
        if (FileLocation.MEMORY.overlaps(loc)) {
            baos = new ByteArrayOutputStream();
            os = baos;
        }
        if ((FileLocation.LOCAL.overlaps(loc) || FileLocation.REMOTE
                .equals(loc))
                && out != null) {
            if (os != null) {
                os = new OutputStreamMultiplexer(os, new FileOutputStream(out));
            }
            else {
                os = new FileOutputStream(out);
            }
        }
        if (os == null) {
            os = new NullOutputStream();
        }

        int len = pin.read(buf);
        while (len != -1) {
            os.write(buf, 0, len);
            len = pin.read(buf);
        }
        os.close();
        if (baos != null) {
            return baos.toString();
        } else {
            return null;
        }
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
}
