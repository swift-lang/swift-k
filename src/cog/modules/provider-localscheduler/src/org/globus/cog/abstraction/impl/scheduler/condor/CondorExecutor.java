//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.condor;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class CondorExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(CondorExecutor.class);

	public CondorExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	protected void writeAttr(String attrName, String arg, Writer wr)
			throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write(arg + String.valueOf(value) + '\n');
		}
	}

	protected void writeScript(Writer wr, String exitcodefile, String stdout,
			String stderr) throws IOException {
		Task task = getTask();
		JobSpecification spec = getSpec();
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		if ("MPI".equals(type)) {
			wr.write("universe = MPI\n");
		}
		else {
			// wr.write("universe = vanilla\n");
			wr.write("universe = vanilla\n");
		}
		writeAttr("count", "machine_count = ", wr);
		if (spec.getStdInput() != null) {
			wr.write("input = " + quote(spec.getStdInput()) + "\n");
		}
		wr.write("output = " + quote(stdout) + '\n');
		wr.write("error = " + quote(stderr) + '\n');
		Iterator i = spec.getEnvironmentVariableNames().iterator();
		if (i.hasNext()) {
			wr.write("environment = ");
		}
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write(';');
		}
		wr.write("\n");

		if (spec.getDirectory() != null) {
			wr.write("initialdir = " + quote(spec.getDirectory()) + "\n");
		}
		wr.write("executable = " + quote(spec.getExecutable()) + "\n");
		List args = spec.getArgumentsAsList();
		if (args != null && args.size() > 0) {
			wr.write("arguments = ");
			i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote((String) i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
		wr.write('\n');
		wr.write("notification = Never\n");
		wr.write("leave_in_queue = TRUE\n");
		wr.write("queue\n");
		wr.close();
	}

	private static final boolean[] TRIGGERS;

	static {
		TRIGGERS = new boolean[128];
		TRIGGERS[' '] = true;
		TRIGGERS['\n'] = true;
		TRIGGERS['\t'] = true;
		TRIGGERS['\\'] = true;
		TRIGGERS['>'] = true;
		TRIGGERS['<'] = true;
		TRIGGERS['"'] = true;
	}

	protected String quote(String s) {
		if ("".equals(s)) {
			return "";
		}
		boolean quotes = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 128 && TRIGGERS[c]) {
				quotes = true;
				break;
			}
		}
		if (!quotes) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		if (quotes) {
			sb.append('\\');
			sb.append('"');
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"' || c == '\\') {
				sb.append('\\');
			}
			sb.append(c);
		}
		if (quotes) {
			sb.append('\\');
			sb.append('"');
		}
		return sb.toString();
	}

	protected String replaceVars(String str) {
		StringBuffer sb = new StringBuffer();
		boolean escaped = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\') {
				if (escaped) {
					sb.append('\\');
				}
				else {
					escaped = true;
				}
			}
			else {
				if (c == '$' && !escaped) {
					if (i == str.length() - 1) {
						sb.append('$');
					}
					else {
						int e = str.indexOf(' ', i);
						if (e == -1) {
							e = str.length();
						}
						String name = str.substring(i + 1, e);
						Object attr = getSpec().getAttribute(name);
						if (attr != null) {
							sb.append(attr.toString());
						}
						else {
							sb.append('$');
							sb.append(name);
						}
						i = e;
					}
				}
				else {
					sb.append(c);
				}
				escaped = false;
			}
		}
		return sb.toString();
	}

	protected String getName() {
		return "Condor";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, null, executor);
	}

	private static QueuePoller poller;

	protected AbstractQueuePoller getQueuePoller() {
		synchronized (CondorExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}

	protected String parseSubmitCommandOutput(String out) throws IOException {
		if (out.endsWith(".")) {
			out = out.substring(0, out.length() - 1);
		}
		int index = out.lastIndexOf(" ");
		return out.substring(index + 1);
	}

	public void cancel() throws TaskSubmissionException {
		String jobid = getJobid();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Marking job " + jobid
						+ " as removable from queue");
			}
			Process p = Runtime.getRuntime().exec(
					new String[] {
							getProperties()
									.getProperty(Properties.CONDOR_QEDIT),
							jobid, "LeaveJobInQueue", "FALSE" });
			int ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully marked " + jobid
							+ " as removable from queue");
				}
			}
			else {
				logger.warn("Failed makr job " + jobid
						+ " as removable from queue: "
						+ getOutput(p.getInputStream()));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Cancelling job " + jobid);
			}

			p = Runtime.getRuntime().exec(
					new String[] { getProperties().getRemoveCommand(), jobid });
			ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully cancelled job " + jobid);
				}
			}
			else {
				logger.warn("Failed to cancel job " + jobid + ": "
						+ getOutput(p.getInputStream()));
			}
		}
		catch (Exception e) {
			logger.warn("Failed to cancel job " + jobid, e);
		}
	}
}
