//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 10, 2009
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.ietf.jgss.GSSException;

public abstract class AbstractExecutor implements ProcessListener {
	public static final Logger logger = Logger
			.getLogger(AbstractExecutor.class);

	private File script;
	private String stdout, stderr, exitcode, jobid;
	private JobSpecification spec;
	private Task task;
	private ProcessListener listener;

	protected AbstractExecutor(Task task, ProcessListener listener) {
		this.task = task;
		this.spec = (JobSpecification) task.getSpecification();
		this.listener = listener;
	}

	public void start() throws AuthorizationException, GSSException,
			IOException, ProcessException {
		File scriptdir = new File(System.getProperty("user.home")
				+ File.separatorChar + ".globus" + File.separatorChar
				+ "scripts");
		scriptdir.mkdirs();
		if (!scriptdir.exists()) {
			throw new IOException("Failed to create script directory ("
					+ scriptdir + ")");
		}
		script = File.createTempFile(getName(), ".submit", scriptdir);
		script.deleteOnExit();
		stdout = spec.getStdOutput() == null ? script.getAbsolutePath()
				+ ".stdout" : spec.getStdOutput();
		stderr = spec.getStdError() == null ? script.getAbsolutePath()
				+ ".stderr" : spec.getStdError();
		exitcode = script.getAbsolutePath() + ".exitcode";

		if (logger.isDebugEnabled()) {
			logger.debug("Wrote " + getName() + " script to " + script);
		}

		String[] cmdline = buildCommandLine(scriptdir, script, exitcode,
				stdout, stderr);

		if (logger.isDebugEnabled()) {
			logCommandLine(cmdline);
		}
		Process process = Runtime.getRuntime().exec(cmdline, null, null);

		try {
			process.getOutputStream().close();
		}
		catch (IOException e) {
		}

		try {
			int code = process.waitFor();
			process.getErrorStream().close();
			if (code != 0) {
				String errorText = "no error output";
				try {
					errorText = getOutput(process.getErrorStream());
				}
				catch (Exception e) {
					logger.debug("Ignoring exception whilst getting "
							+ getProperties().getSubmitCommandName()
							+ " error text", e);
				}
				throw new ProcessException("Could not submit job ("
						+ getProperties().getSubmitCommandName()
						+ " reported an exit code of " + code + "). "
						+ errorText);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(getProperties().getSubmitCommandName()
						+ " done (exit code " + code + ")");
			}
		}
		catch (InterruptedException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Interrupted exception while waiting for "
						+ getProperties().getSubmitCommandName(), e);
			}
			if (listener != null) {
				listener
						.processFailed("The submission process was interrupted");
			}
		}

		jobid = parseSubmitCommandOutput(getOutput(process.getInputStream()));
		if (logger.isDebugEnabled()) {
			logger.debug("Submitted job with id '" + jobid + "'");
		}
		process.getInputStream().close();

		getQueuePoller().addJob(
				createJob(jobid, stdout, spec.getStdOutputLocation(), stderr,
						spec.getStdErrorLocation(), exitcode, this));
	}

	protected abstract Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor);

	protected void logCommandLine(String[] cmdline) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cmdline.length; i++) {
			sb.append(cmdline[i]);
			if (i < cmdline.length - 1) {
				sb.append(", ");
			}
		}
		logger.debug("Command line: " + sb.toString());
	}

	protected String[] buildCommandLine(File jobdir, File script,
			String exitcode, String stdout, String stderr) throws IOException {
		writeScript(new BufferedWriter(new FileWriter(script)), exitcode,
				stdout, stderr);
		if (logger.isDebugEnabled()) {
			logger.debug("Wrote " + getName() + " script to " + script);
		}

		String[] cmdline = new String[] { getProperties().getSubmitCommand(),
				script.getAbsolutePath() };
		return cmdline;
	}

	public void cancel() throws TaskSubmissionException {
		if (jobid == null) {
			throw new TaskSubmissionException("Can only cancel an active task");
		}
		String[] cmdline = new String[] { getProperties().getRemoveCommand(),
				jobid };
		try {
			System.out.println("Canceling job " + jobid);
			Process process = Runtime.getRuntime().exec(cmdline, null, null);
			int ec = process.waitFor();
			if (ec != 0) {
				throw new TaskSubmissionException("Failed to cancel task. "
						+ getProperties().getRemoveCommandName()
						+ " returned with an exit code of " + ec);
			}
		}
		catch (InterruptedException e) {
			throw new TaskSubmissionException(
					"Thread interrupted while waiting for "
							+ getProperties().getRemoveCommandName()
							+ " to finish");
		}
		catch (IOException e) {
			throw new TaskSubmissionException("Failed to cancel task", e);
		}
	}

	protected abstract AbstractProperties getProperties();

	protected abstract String getName();

	protected String getOutput(InputStream is) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Waiting for output from "
					+ getProperties().getSubmitCommandName());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String out = br.readLine();
		while (out != null) {
			sb.append(out);
			out = br.readLine();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Output from "
					+ getProperties().getSubmitCommandName() + " is: \"" + sb.toString()
					+ "\"");
		}
		return sb.toString();
	}
	
	protected String parseSubmitCommandOutput(String out) throws IOException {
		if ("".equals(out)) {
			throw new IOException(getProperties().getSubmitCommandName()
					+ " returned an empty job ID");
		}
		return out;
	}

	protected abstract void writeScript(Writer wr, String exitcode,
			String stdout, String stderr) throws IOException;

	protected JobSpecification getSpec() {
		return spec;
	}

	protected Task getTask() {
		return task;
	}

	protected void cleanup() {
		if (!getProperties().isDebugEnabled()) {
			script.delete();
			new File(exitcode).delete();
			if (spec.getStdOutput() == null && stdout != null) {
				new File(stdout).delete();
			}
			if (spec.getStdError() == null && stderr != null) {
				new File(stderr).delete();
			}
		}
	}

	public void processCompleted(int exitCode) {
		cleanup();
		if (listener != null) {
			listener.processCompleted(exitCode);
		}
	}

	public void processFailed(String message) {
		cleanup();
		if (listener != null) {
			listener.processFailed(message);
		}
	}

	public void statusChanged(int status) {
		if (listener != null) {
			listener.statusChanged(status);
		}
	}

	public void stderrUpdated(String stderr) {
		if (listener != null) {
			listener.stderrUpdated(stderr);
		}
	}

	public void stdoutUpdated(String stdout) {
		if (listener != null) {
			listener.stdoutUpdated(stdout);
		}
	}

	public void processFailed(Exception e) {
		cleanup();
		if (listener != null) {
			listener.processFailed(e);
		}
	}

	protected void error(String message) {
		listener.processFailed(message);
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	protected abstract AbstractQueuePoller getQueuePoller();

	public String getJobid() {
		return jobid;
	}
}
