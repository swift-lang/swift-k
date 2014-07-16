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
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class CobaltExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(CobaltExecutor.class);

	private final String cqsub;
	private final Pattern exitcodeRegexp;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	public CobaltExecutor(Task task, ProcessListener listener) {
		super(task, listener);
		this.cqsub = Properties.getProperties().getSubmitCommand();
		this.exitcodeRegexp = Pattern.compile(Properties.getProperties()
				.getExitcodeRegexp());
	}

	@Override
    protected void validate(Task task) {
	    JobSpecification spec = (JobSpecification) task.getSpecification();
	    if (spec.getAttribute("alcfbgpnat") != null) {
            spec.addEnvironmentVariable("ZOID_ENABLE_NAT", "true");
        }
        super.validate(task);
    }

    @Override
	protected Job createJob(String jobid,
                            String stdout,
                            FileLocation stdOutputLocation,
                            String stderr,
                            FileLocation stdErrorLocation,
                            String exitcode,
                            AbstractExecutor executor) {
		return new CobaltJob(jobid, stdout, stderr,
		                     getSpec().getStdOutput(),
		                     stdOutputLocation,
		                     getSpec().getStdError(),
		                     stdErrorLocation, exitcodeRegexp, this);
	}

	@Override
	protected String getName() {
		return "Cobalt";
	}

	@Override
	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	@Override
	protected void writeScript(Writer wr, String exitcode, String stdout,
			String stderr) throws IOException {
	}

	protected void addAttr(String attrName, String option, List<String> l) {
		addAttr(attrName, option, l, null);
	}

	protected void addAttr(String attrName, String option, List<String> l, boolean round) {
		addAttr(attrName, option, l, null, round);
	}

	protected void addAttr(String attrName, String option, List<String> l, String defval) {
		addAttr(attrName, option, l, defval, false);
	}

	protected void addAttr(String attrName, String option, List<String> l,
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

	@Override
	protected String[] buildCommandLine(File jobdir, File script,
			String exitcode, String stdout, String stderr) throws IOException {
		List<String> result = new ArrayList<String>();
		result.add(cqsub);
		Collection<String> names = getSpec().getEnvironmentVariableNames();
		if (names != null && names.size() > 0) {
			result.add("-e");
			StringBuffer sb = new StringBuffer();
			Iterator<String> i = names.iterator();
			while (i.hasNext()) {
				String name = i.next();
				sb.append(name);
				sb.append('=');
				sb.append(quote(getSpec().getEnvironmentVariable(name)));
				if (i.hasNext()) {
					sb.append(':');
				}
			}
			result.add(sb.toString());
		}
		addAttr("mode", "-m", result);
		// We're gonna treat this as the node count
		addAttr("count", "-c", result, true);
		addAttr("hostCount", "-n", result, "1", true);
		addAttr("project", "-p", result);
		addAttr("queue", "-q", result);
		addAttr("kernelprofile", "-k", result);
		// cqsub seems to require both the node count and time args
		addAttr("maxwalltime", "-t", result, "10");
		if (getSpec().getDirectory() != null) {
			result.add("-C");
			result.add(getSpec().getDirectory());
		}
		result.add("-o");
		result.add(stdout);
		result.add("-E");
		result.add(stderr);
		result.add(getSpec().getExecutable());
		result.addAll(getSpec().getArgumentsAsList());
		if (logger.isDebugEnabled()) {
			logger.debug("cqsub command: " + result);
		}
		return result.toArray(EMPTY_STRING_ARRAY);
	}

	@Override
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

	@Override
	protected void cleanup() {
		super.cleanup();
		new File(getStdout()).delete();
		new File(getStderr()).delete();
	}

	private static AbstractQueuePoller poller;

	@Override
	protected AbstractQueuePoller getQueuePoller() {
		synchronized(CobaltExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
