/*
 * Created on Dec 6, 2006
 */
package org.griphyn.vdl.karajan.functions;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;
import org.griphyn.vdl.karajan.VDL2ErrorTranslator;

public class ProcessBulkErrors extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(ProcessBulkErrors.class);

	public static final Arg MESSAGE = new Arg.Positional("message");
	public static final Arg ERRORS = new Arg.Positional("errors");
	public static final Arg ONSTDOUT = new Arg.Optional("onstdout", Boolean.FALSE);

	static {
		setArguments(ProcessBulkErrors.class, new Arg[] { MESSAGE, ERRORS, ONSTDOUT });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String message = TypeUtil.toString(MESSAGE.getValue(stack));
		boolean onStdout = TypeUtil.toBoolean(ONSTDOUT.getValue(stack));
		List l = TypeUtil.toList(ERRORS.getValue(stack));

		VDL2ErrorTranslator translator = VDL2ErrorTranslator.getDefault();

		Map count = new HashMap();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			ExecutionException ex = (ExecutionException) i.next();
			if (ex.getCause() instanceof ConcurrentModificationException) {
				ex.printStackTrace();
			}
			if (logger.isDebugEnabled()) {
				logger.debug(ex);
			}
			String msg = getMessageChain(ex);
			String tmsg = translator.translate(msg);
			if (tmsg == null) {
				if (msg != null && msg.startsWith("VDL2: ")) {
					tmsg = ex.getMessage().substring(6);
				}
				else {
					tmsg = msg;
				}
			}
			tmsg = tmsg.trim();
			if (count.containsKey(tmsg)) {
				Integer j = (Integer) count.get(tmsg);
				count.put(tmsg, new Integer(j.intValue() + 1));
			}
			else {
				count.put(tmsg, new Integer(1));
			}
		}
		Arg.Channel channel = onStdout ? STDOUT : STDERR;
		if (count.size() != 0) {
			channel.ret(stack, message + "\n");
			i = count.entrySet().iterator();
			int k = 1;
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Integer j = (Integer) e.getValue();
				if (j.intValue() == 1) {
					channel.ret(stack, k + ". " + e.getKey() + "\n");
				}
				else {
					channel.ret(stack, k + ". " + e.getKey() + " (" + j.intValue() + " times)\n");
				}
				k++;
			}
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public static String getMessageChain(Throwable e) {
		StringBuffer sb = new StringBuffer();
		String prev = null;
        boolean first = true;
		while (e != null) {
			String msg = e.getMessage();
			if (msg != null && (prev == null || prev.indexOf(msg) == -1)) {
                if (!first) {
                    sb.append("\nCaused by:\n\t");
                }
                else {
                    first = false;
                }
				sb.append(msg);
                prev = msg;
			}
            e = e.getCause();
		}
		return sb.toString();
	}
}
