//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.pbs;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class PBSExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(PBSExecutor.class);

	public PBSExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	protected void writeAttr(String attrName, String arg, Writer wr)
			throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write("#PBS " + arg + String.valueOf(value) + '\n');
		}
	}

	protected void writeWallTime(Writer wr) throws IOException {
		Object walltime = getSpec().getAttribute("maxwalltime");
		if (walltime != null) {
			wr.write("#PBS -l walltime="
					+ WallTime.normalize(walltime.toString(), "pbs-native")
					+ '\n');
		}
	}

	protected void writeScript(Writer wr, String exitcodefile, String stdout,
			String stderr) throws IOException {
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();
		wr.write("#PBS -S /bin/sh\n");
		wr.write("#PBS -N " + task.getName() + '\n');
		wr.write("#PBS -m n\n");
		writeAttr("project", "-A ", wr);
		if ("true".equals
		        (properties.getProperty(Properties.USE_MPPWIDTH)))
		    writeAttr("count", "-l mppwidth=", wr);
		else
		    writeAttr("count", "-l nodes=", wr);
		writeWallTime(wr);
		writeAttr("queue", "-q ", wr);
		if (spec.getStdInput() != null) {
			throw new IOException("The PBSlocal provider cannot redirect STDIN");
		}
		wr.write("#PBS -o " + quote(stdout) + '\n');
		wr.write("#PBS -e " + quote(stderr) + '\n');
		Iterator i = spec.getEnvironmentVariableNames().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write('\n');
		}
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		if (type != null) {
			String wrapper = 
			    properties.getProperty("wrapper." + type);
			if (logger.isDebugEnabled()) {
				logger.debug("Wrapper: " + wrapper);
			}
			if (wrapper != null) {
				wrapper = replaceVars(wrapper);
				wr.write(wrapper);
				wr.write(' ');
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Wrapper after variable substitution: " + wrapper);
			}
		}
		if (spec.getDirectory() != null) {
			wr.write("cd " + quote(spec.getDirectory()) + " && ");
		}
		wr.write(quote(spec.getExecutable()));
		List args = spec.getArgumentsAsList();
		if (args != null && args.size() > 0) {
			wr.write(' ');
			i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote((String) i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
		wr.write('\n');

		wr.write("/bin/echo $? >" + exitcodefile + '\n');
		wr.close();
	}

	private static final boolean[] TRIGGERS;

	static {
		TRIGGERS = new boolean[128];
		TRIGGERS[' '] = true;
		TRIGGERS['\n'] = true;
		TRIGGERS['\t'] = true;
		TRIGGERS['|'] = true;
		TRIGGERS['\\'] = true;
		TRIGGERS['>'] = true;
		TRIGGERS['<'] = true;
	}

	protected String quote(String s) {
		if ("".equals(s)) {
			return "\"\"";
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
		return "PBS";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, exitcode, executor);
	}
	
	private static QueuePoller poller;

	protected AbstractQueuePoller getQueuePoller() {
		synchronized(PBSExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
