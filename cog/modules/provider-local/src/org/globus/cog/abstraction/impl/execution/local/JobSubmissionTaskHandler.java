//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * @author Kaizar Amin (amin@mcs.anl.gov)
 * 
 */
public class JobSubmissionTaskHandler implements DelegatedTaskHandler, Runnable {
	private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

	private Task task = null;
	private Thread thread = null;
	private Process process;
	private volatile boolean killed;

	public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		if (this.task != null) {
			throw new TaskSubmissionException(
					"JobSubmissionTaskHandler cannot handle two active jobs simultaneously");
		}
		else {
			this.task = task;
			JobSpecification spec;
			try {
				spec = (JobSpecification) this.task.getSpecification();
			}
			catch (Exception e) {
				throw new IllegalSpecException("Exception while retreiving Job Specification", e);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug(spec.toString());
			}

			try {
				this.thread = new Thread(this);
				// check if the task has not been canceled after it was
				// submitted for execution
				if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
					this.task.setStatus(Status.SUBMITTED);
					this.thread.start();
					if (spec.isBatchJob()) {
						this.task.setStatus(Status.COMPLETED);
					}
				}
			}
			catch (Exception e) {
				Status newStatus = new StatusImpl();
				Status oldStatus = this.task.getStatus();
				newStatus.setPrevStatusCode(oldStatus.getStatusCode());
				newStatus.setStatusCode(Status.FAILED);
				newStatus.setException(e);
				this.task.setStatus(newStatus);
				throw new TaskSubmissionException("Cannot submit job", e);
			}
		}
	}

	public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
	}

	public void cancel() throws InvalidSecurityContextException, TaskSubmissionException {
		killed = true;
		process.destroy();
		this.task.setStatus(Status.CANCELED);
	}

	public void run() {
		try {
			// TODO move away from the multi-threaded approach
			JobSpecification spec = (JobSpecification) this.task.getSpecification();

			File dir = null;
			if (spec.getDirectory() != null) {
				dir = new File(spec.getDirectory());
			}

			process = Runtime.getRuntime().exec(buildCmdArray(spec), buildEnvp(spec), dir);

			if (spec.getStdInput() != null) {
				OutputStream out = process.getOutputStream();
				
				File stdin;
				if (dir != null) {
					stdin = new File(dir, spec.getStdInput());
				}
				else {
					stdin = new File(spec.getStdInput());
				}

				FileInputStream file = new FileInputStream(stdin);
				InputStreamReader inReader = new InputStreamReader(file);
				BufferedReader inBuffer = new BufferedReader(inReader);
				String message = inBuffer.readLine();
				while (message != null) {
					out.write(message.getBytes());
					message = inBuffer.readLine();
				}
				inBuffer.close();
			}

			// process output
			InputStreamReader inReader = new InputStreamReader(process.getInputStream());
			BufferedReader inBuffer = new BufferedReader(inReader);
			String message = inBuffer.readLine();
			String output = message;
			while (message != null) {
				message = inBuffer.readLine();
				if (message != null) {
					output += message + "\n";
				}
			}
			if (spec.getStdOutput() == null) {
				// redirect output to the stdOutput of task
				this.task.setStdOutput(output);
				logger.debug("STDOUT from job: " + output);
			}
			else {
				// redirect it to the specified file
				File stdout;
				if (dir != null) {
					stdout = new File(dir, spec.getStdOutput());
				}
				else {
					stdout = new File(spec.getStdOutput());
				}
				FileWriter writer = new FileWriter(stdout);
				if (output != null) {
					writer.write(output);
					writer.flush();
				}
				writer.close();
			}

			// process error
			inReader = new InputStreamReader(process.getErrorStream());
			inBuffer = new BufferedReader(inReader);
			message = inBuffer.readLine();
			output = message;
			while (message != null) {
				message = inBuffer.readLine();
				if (message != null) {
					output += message + "\n";
				}
			}
			if (spec.getStdError() == null) {
				// redirect output to the stdError of task
				this.task.setStdError(output);
				logger.debug("STDERR from job: " + output);
			}
			else {
				// redirect it to the specified file
				File stderr;
				if (dir != null) {
					stderr = new File(dir, spec.getStdError());
				}
				else {
					stderr = new File(spec.getStdError());
				}
				FileWriter writer = new FileWriter(stderr);
				if (output != null) {
					writer.write(output);
					writer.flush();
				}
				writer.close();
			}

			if (spec.isBatchJob()) {
				return;
			}

			int exitCode = process.waitFor();
			logger.debug("Exit code was " + exitCode);
			if (killed) {
				return;
			}
			if (exitCode == 0) {
				this.task.setStatus(Status.COMPLETED);
			}
			else {
				throw new Exception("Job failed with an exit code of " + exitCode + "\n" + output);
			}
		}
		catch (Exception e) {
			if (killed) {
				return;
			}
			logger.debug("Exception while running local executable", e);
			Status newStatus = new StatusImpl();
			Status oldStatus = this.task.getStatus();
			newStatus.setPrevStatusCode(oldStatus.getStatusCode());
			newStatus.setStatusCode(Status.FAILED);
			newStatus.setException(e);
			this.task.setStatus(newStatus);
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