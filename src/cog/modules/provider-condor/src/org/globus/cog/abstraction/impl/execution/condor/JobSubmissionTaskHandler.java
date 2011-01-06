//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.condor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler
        implements Runnable {
    private static Logger logger = Logger
            .getLogger(JobSubmissionTaskHandler.class);

    private Task task = null;
    private Thread thread = null;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        checkAndSetTask(task);
        JobSpecification spec;
        try {
            spec = (JobSpecification) this.task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }

        try {
            this.thread = new Thread(this);
            // check if the task has not been canceled after it was
            // submitted for execution
            if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
                this.thread.start();

                /*
                 * if the job is batch job or the description file is specified,
                 * and the condor_submit was successful, then the task is
                 * completed
                 */
                if ((spec.isBatchJob() || spec.getAttribute("descriptionFile") != null)
                        && this.task.getStatus().getStatusCode() != Status.FAILED) {
                    this.task.setStatus(Status.COMPLETED);
                }
            }
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Cannot submit job", e);
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        this.task.setStatus(new StatusImpl(Status.CANCELED, message, null));
    }

    public void run() {
        try {
            JobSpecification spec = (JobSpecification) this.task
                    .getSpecification();

            File descriptionFile = DescriptionFileGenerator.generate(this.task);
            String cmd = "condor_submit " + descriptionFile.getAbsolutePath();
            Process process = Runtime.getRuntime().exec(cmd);

            // process output
            InputStreamReader inReader = new InputStreamReader(process
                    .getInputStream());
            BufferedReader inBuffer = new BufferedReader(inReader);
            String message = inBuffer.readLine();
            String output = message;
            while (message != null) {
                message = inBuffer.readLine();
                if (message != null) {
                    output += message + "\n";
                }
            }
            // redirect output to the stdOutput of condor_submit
            this.task.setStdOutput(output);
            logger.debug("STDOUT from condor_submit: " + output);

            // process error
            inReader = new InputStreamReader(process.getInputStream());
            inBuffer = new BufferedReader(inReader);
            message = inBuffer.readLine();
            output = message;
            while (message != null) {
                message = inBuffer.readLine();
                if (message != null) {
                    output += message + "\n";
                }
            }

            // redirect output to the stdError of condor_submit
            this.task.setStdError(output);
            logger.debug("STDERR from condor_submit: " + output);

            int exitCode = process.waitFor();
            logger.debug("Exit code was " + exitCode);
            if (exitCode != 0) {
                throw new Exception(
                        "condor_submit failed with an exit code of " + exitCode);
            }

            if (spec.isBatchJob()
                    || spec.getAttribute("descriptionFile") != null) {
                return;
            }

            // if not a batch job and if no special description file specified
            // the task status is monitored via the log file
            LogReader logReader = new LogReader((String) spec
                    .getAttribute("log"), this.task);
            Thread thread = new Thread(logReader);
            thread.start();

        }
        catch (Exception e) {
            logger.debug("Exception while submitting the condor job", e);
            failTask(null, e);
        }
    }
}
