//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.parser.WrapperNode;

public class RestartLog extends InternalFunction {
	public static final Logger logger = Logger.getLogger(RestartLog.class);
	
	public static final String LOG_DATA = "RESTART_LOG:DATA";
	
	public static final int MAX_INDEX = 16384;
		
	private ArgRef<String> resume;
	private ArgRef<String> name;
	private ChannelRef<String> c_restartLog;
	
	private Node body;
	
	private VarRef<Context> context;
	private VarRef<String> fileName;
	private VarRef<String> cwd;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(optional("resume", null), optional("name", null), block("body")));
	}
	
	@Override
	protected void addLocals(Scope scope) {
		context = scope.getVarRef("#context");
		fileName = scope.getVarRef("#filename");
		cwd = scope.getVarRef("CWD");
		super.addLocals(scope);
	}
	
	
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var.Channel rl = scope.addChannel("restartLog");
		rl.setNoBuffer(true);
		rl.appendDynamic();
		c_restartLog = new ChannelRef.Dynamic<String>("restartLog", rl.getIndex());
		super.compileBlocks(w, sig, blocks, scope);
	}


	@Override
	public void runBody(LWThread thr) {
		if (body == null) {
			return;
		}
		int i = thr.checkSliceAndPopState();
		int fc = thr.popIntState();
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					fc = stack.frameCount();
					createLog(stack);
					i++;
				case 1:
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					deleteLog(stack);
			}
		}
		catch (ExecutionException e) {
			stack.dropToFrame(fc);
			closeLog(stack);
			throw e;
		}
		catch (Yield y) {
			y.getState().push(fc);
			y.getState().push(i);
			throw y;
		}
	}

	protected void createLog(Stack stack) {
		String resume = this.resume.getValue(stack);
		if (resume == null) {
			resume = getResumeConstant(stack);
		}
		String name = this.name.getValue(stack);

		if (resume != null) {
			resume(stack, new File(resume));
		}
		else {
			create(stack, name);
		}
	}

	private String getResumeConstant(Stack stack) {
		List<String> args = this.context.getValue(stack).getArguments();
		for (String arg : args) {
			if (arg.startsWith("-rlog:resume=")) {
				String rlog = arg.substring("-rlog:resume=".length());
				return rlog;
			}
		}
		
		return null;
	}

	protected void resume(Stack stack, File log) throws ExecutionException {
		if (log.exists()) {
		    Map<LogEntry, Object> logData;
			try {
				logData = parseLog(log);
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
				c_restartLog.set(stack, new LogChannelOperator(logffw));
				context.getValue(stack).setAttribute(LOG_DATA, logData);
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

	protected void create(Stack stack, String name) throws ExecutionException {
		FlushableLockedFileWriter logffw = null;
		if (name == null) {
			name = fileName.getValue(stack);
		}
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
                        String restartLogFilename = System.getProperty("restart.log.name");
                        if(restartLogFilename == null) {
                                String index = "." + String.valueOf(i);
                                restartLogFilename = name + index + ".rlog";
                        }
                        File f = new File(restartLogFilename);

			if (f.exists()) {
				if (i == MAX_INDEX) {
					throw new ExecutionException(this, "Maximum restart log index reached");
				}
				continue;
			}
			try {
				logffw = new FlushableLockedFileWriter(f, true);
				writeDate(logffw, "# Log file created ");
			}
			catch (IOException e) {
				throw new ExecutionException(this, "Exception caught trying to get exclusive lock on "
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
		Map<LogEntry, Object> m = Collections.emptyMap();
		if (logffw == null) {
			throw new ExecutionException(this, "Could not create unique log file");
		}
		c_restartLog.set(stack, new LogChannelOperator(logffw));
		context.getValue(stack).setAttribute(LOG_DATA, m);
	}

	private void writeDate(FlushableLockedFileWriter logffw, String prefix) throws IOException {
		logffw.write(prefix);
		logffw.write(Calendar.getInstance().getTime().toString());
		logffw.write('\n');
	}

	private void closeLog(Stack stack) throws ExecutionException {
		try {
			c_restartLog.get(stack).close();
		}
		catch (Exception e) {
			logger.warn("Failed to close log file", e);
		}
	}

	private void deleteLog(Stack stack) throws ExecutionException {
		closeLog(stack);
		File logf = ((LogChannelOperator) c_restartLog.get(stack)).getFile();
		if (!logf.delete()) {
			logger.warn("Faile to delete log file (" + logf.getAbsolutePath()
					+ "). Please delete the file manually.");
		}
	}

	private Map<LogEntry, Object> parseLog(File f) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
			HashMap<LogEntry, Object> data = new HashMap<LogEntry, Object>();
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
						@SuppressWarnings("unchecked")
						List<String> old = (List<String>) data.get(entry);
						if (old == null) {
							old = new LinkedList<String>();
							data.put(entry, old);
						}
						old.add(entry.getValue());
					}
				}
				catch (ExecutionException e) {
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
