//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.impl.scheduler.pbs.PBSExecutor;
import org.globus.cog.abstraction.impl.scheduler.pbs.QueuePoller;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.Task;

public class CobaltExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(CobaltExecutor.class);

	private String cqsub;
	private Pattern exitcodeRegexp;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	public CobaltExecutor(Task task, ProcessListener listener) {
		super(task, listener);
		this.cqsub = Properties.getProperties().getSubmitCommand();
		this.exitcodeRegexp = Pattern.compile(Properties.getProperties()
				.getExitcodeRegexp());
	}
	
	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new CobaltJob(jobid, stdout, stderr, getSpec().getStdOutput(), stdOutputLocation, 
				getSpec().getStdError(), stdErrorLocation, exitcodeRegexp, this);
	}

	protected String getName() {
		return "Cobalt";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected void writeScript(Writer wr, String exitcode, String stdout,
			String stderr) throws IOException {
	}

	protected void addAttr(String attrName, String option, List l) {
		addAttr(attrName, option, l, null);
	}

	protected void addAttr(String attrName, String option, List l, boolean round) {
		addAttr(attrName, option, l, null, round);
	}

	protected void addAttr(String attrName, String option, List l, String defval) {
		addAttr(attrName, option, l, defval, false);
	}

	protected void addAttr(String attrName, String option, List l,
			String defval, boolean round) {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			if (round) {
				value = round(value);
			}
			l.add(option);
			l.add(String.valueOf(value));
		}
		else if (defval != null) {
			l.add(option);
			l.add(defval);
		}
	}

	protected Object round(Object value) {
		if (value instanceof Number) {
			return new Integer(((Number) value).intValue());
		}
		else {
			return value;
		}
	}

	protected String[] buildCommandLine(File jobdir, File script,
			String exitcode, String stdout, String stderr) throws IOException {
		List l = new ArrayList();
		l.add(cqsub);
		Collection names = getSpec().getEnvironmentVariableNames();
		if (names != null && names.size() > 0) {
			l.add("-e");
			StringBuffer sb = new StringBuffer();
			Iterator i = names.iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				sb.append(name);
				sb.append('=');
				sb.append(quote(getSpec().getEnvironmentVariable(name)));
				if (i.hasNext()) {
					sb.append(':');
				}
			}
			l.add(sb.toString());
		}
		addAttr("mode", "-m", l);
		// We're gonna treat this as the node count
		addAttr("count", "-c", l, true);
		addAttr("hostCount", "-n", l, "1", true);
		addAttr("project", "-p", l);
		addAttr("queue", "-q", l);
		addAttr("kernelprofile", "-k", l);
		// cqsub seems to require both the node count and time args
		addAttr("maxwalltime", "-t", l, "10");
		if (getSpec().getDirectory() != null) {
			l.add("-C");
			l.add(getSpec().getDirectory());
		}
		l.add("-o");
		l.add(stdout);
		l.add("-E");
		l.add(stderr);
		l.add(getSpec().getExecutable());
		l.addAll(getSpec().getArgumentsAsList());
		if (logger.isDebugEnabled()) {
			logger.debug("Cqsub cmd line: " + l);
		}
		return (String[]) l.toArray(EMPTY_STRING_ARRAY);
	}

	protected String quote(String s) {
		boolean quotes = false;
		if (s.indexOf(' ') != -1) {
			quotes = true;
		}
		StringBuffer sb = new StringBuffer();
		if (quotes) {
			sb.append('"');
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"' || c == '\\') {
				sb.append('\\');
				break;
			}
			sb.append(c);
		}
		if (quotes) {
			sb.append('"');
		}
		return sb.toString();
	}

	protected void cleanup() {
		super.cleanup();
		new File(getStdout()).delete();
		new File(getStderr()).delete();
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
