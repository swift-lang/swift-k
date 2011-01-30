/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class Log extends AbstractSequentialWithArguments {
	public static final Arg LEVEL = new Arg.Positional("level");
	public static final Arg MESSAGE = new Arg.Optional("message", null);

	static {
		setArguments(Log.class, new Arg[] { LEVEL, MESSAGE, Arg.VARGS });
	}

	private static Map loggers = new HashMap();

	public static Logger getLogger(String cls) {
		Logger logger;
		synchronized (loggers) {
			logger = (Logger) loggers.get(cls);
			if (logger == null) {
				logger = Logger.getLogger("swift." + cls);
				loggers.put(cls, logger);
			}
		}
		return logger;
	}

	private static Map priorities;

	static {
		priorities = new HashMap();
		priorities.put("debug", Level.DEBUG);
		priorities.put("info", Level.INFO);
		priorities.put("warn", Level.WARN);
		priorities.put("error", Level.ERROR);
		priorities.put("fatal", Level.FATAL);
	}

	public static Level getLevel(String lvl) {
		return (Level) priorities.get(lvl);
	}

	protected void post(VariableStack stack) throws ExecutionException {
		String cls;
		FlowElement fe;
		try{
			fe = (FlowElement) stack.getDeepVar(Trace.ELEMENT);
		}
		catch (VariableNotFoundException e) {
			fe = null;
		}
		if (fe != null) {
			cls = fe.getElementType();
		}
		else {
			cls = "unknown";
		}
		Level lvl = getLevel((String) LEVEL.getValue(stack));
		Logger logger = getLogger(cls);
		if (logger.isEnabledFor(lvl)) {
		    Object smsg = MESSAGE.getValue(stack);
		    if (smsg != null) {
		        logger.log(lvl, smsg);
		    }
		    else {
		        Object[] msg = Arg.VARGS.asArray(stack);
		        StringBuilder sb = new StringBuilder();
		        for (int i = 0; i < msg.length; i++) {
		            sb.append(TypeUtil.toString(msg[i]));
		        }
		        logger.log(lvl, sb.toString());
		    }
		}
		super.post(stack);
	}
}
