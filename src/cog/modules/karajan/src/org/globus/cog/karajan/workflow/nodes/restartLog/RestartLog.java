//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.workflow.nodes.restartLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;

public class RestartLog extends PartialArgumentsContainer {
	public static final Logger logger = Logger.getLogger(RestartLog.class);

	public static final Arg A_RESUME = new Arg.Optional("resume");
	public static final Arg A_NAME = new Arg.Optional("name", null);
	public static final Arg.Channel LOG_CHANNEL = new Arg.Channel("restartlog");

	static {
		setArguments(RestartLog.class, new Arg[] { A_RESUME, A_NAME });
	}

	public static final String LOG_DATA = "##logdata";
	public static final String RESUME = "#rlog:resume";

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String resume = TypeUtil.toString(A_RESUME.getValue(stack, getResumeConstant(stack)));
		String name = TypeUtil.toString(A_NAME.getValue(stack));
		super.partialArgumentsEvaluated(stack);

		if (resume != null) {
			resume(stack, new File(resume));
		}
		else {
			create(stack, name);
		}
	}

	private String getResumeConstant(VariableStack stack) {
		if (stack.firstFrame().isDefined(RESUME)) {
			return (String) stack.firstFrame().getVar(RESUME);
		}
		else {
			List args = (List) stack.firstFrame().getVar(ExecutionContext.CMDLINE_ARGS);
			Iterator i = args.iterator();
			while (i.hasNext()) {
				String arg = (String) i.next();
				if (arg.startsWith("-rlog:resume=")) {
					String rlog = arg.substring("-rlog:resume=".length());
					stack.firstFrame().setVar(RESUME, rlog);
					return rlog;
				}
			}
		}
		stack.firstFrame().setVar(RESUME, null);
		return null;
	}

	protected void resume(VariableStack stack, File log) throws ExecutionException {
		if (log.exists()) {
			try {
				stack.setVar(LOG_DATA, parseLog(log));
			}
			catch (FileNotFoundException e) {
				throw new ExecutionException("Log file was deleted", e);
			}
			catch (IOException e) {
				throw new ExecutionException("I/O Exception caught while reading the log file ("
						+ log.getAbsolutePath() + ")", e);
			}
			try {
				FlushableLockedFileWriter logffw = new FlushableLockedFileWriter(log, true);
				if (!logffw.isLocked()) {
					throw new ExecutionException("Could not aquire exclusive lock on log file: "
							+ log.getAbsolutePath());
				}
				writeDate(logffw, "# Log file updated ");
				startRest(stack, logffw);
			}
			catch (IOException e) {
				throw new ExecutionException("Exception caught while creating log file", e);
			}
		}
		else {
			throw new ExecutionException("The resume log file does not exist: "
					+ log.getAbsolutePath());
		}
	}

	protected void create(VariableStack stack, String name) throws ExecutionException {
		FlushableLockedFileWriter logffw = null;
		if (name == null) {
			name = new File(stack.getExecutionContext().getTree().getName()).getName();
		}
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			String index = "." + String.valueOf(i);
			File f = new File(stack.getExecutionContext().getCwd() + File.separator + name + index
					+ ".rlog");
			if (f.exists()) {
				continue;
			}
			try {
				logffw = new FlushableLockedFileWriter(f, true);
				writeDate(logffw, "# Log file created ");
			}
			catch (IOException e) {
				throw new ExecutionException("Exception caught trying to get exclusive lock on "
						+ f.getAbsolutePath(), e);
			}
			if (logffw.isLocked()) {
				break;
			}
			else if (!logffw.lockExists()) {
				logger.warn("Failed to acquire exclusive lock on log file.");
				break;
			}
		}
		stack.setVar(LOG_DATA, Collections.EMPTY_MAP);
		if (logffw == null) {
			throw new ExecutionException("Could not create unique log file");
		}
		startRest(stack, logffw);
	}

	private void writeDate(FlushableLockedFileWriter logffw, String prefix) throws IOException {
		logffw.write(prefix);
		logffw.write(Calendar.getInstance().getTime().toString());
		logffw.write('\n');
	}

	protected void startRest(VariableStack stack, FlushableLockedFileWriter logffw)
			throws ExecutionException {
		LOG_CHANNEL.create(stack, new LogVargOperator(logffw));
		startRest(stack);
	}

	protected void post(VariableStack stack) throws ExecutionException {
		deleteLog(stack);
		super.post(stack);
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
		closeLog(stack);
		super._finally(stack);
	}

	private void closeLog(VariableStack stack) throws ExecutionException {
		try {
			Object channel = LOG_CHANNEL.get(stack);
			if (channel instanceof LogVargOperator) {
				((LogVargOperator) channel).close();
			}
		}
		catch (IOException e) {
			logger.warn("Failed to close log file", e);
		}
	}

	private void deleteLog(VariableStack stack) throws ExecutionException {
		closeLog(stack);
		File logf = ((LogVargOperator) LOG_CHANNEL.get(stack)).getFile();
		if (!logf.delete()) {
			logger.warn("Faile to delete log file (" + logf.getAbsolutePath()
					+ "). Please delete the file manually.");
		}
	}

	private HashMap parseLog(File f) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
			HashMap data = new HashMap();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				try {
					LogEntry entry = LogEntry.parse(line);
					if (entry.getValue() == null) {
						MutableInteger old = (MutableInteger) data.get(entry);
						if (old == null) {
							data.put(entry, new MutableInteger(1));
						}
						else {
							old.inc();
						}
					}
					else {
						List old = (List) data.get(entry);
						if (old == null) {
							old = new LinkedList();
							data.put(entry, old);
						}
						old.add(entry.getValue());
					}
				}
				catch (IllegalArgumentException e) {
					logger.warn("Invalid line in log file: " + line);
				}
			}
			return data;
		}
		finally {
			try {
				br.close();
			}
			catch (IOException e) {
				logger.warn("Could not close log file");
			}
		}
	}
}
