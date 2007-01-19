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

	public static final Arg ERRORS = new Arg.Positional("errors");

	static {
		setArguments(ProcessBulkErrors.class, new Arg[] { ERRORS });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		VDL2ErrorTranslator translator = VDL2ErrorTranslator.getDefault();

		List l = TypeUtil.toList(ERRORS.getValue(stack));
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
			String msg = ex.toString();
			String tmsg = translator.translate(msg);
			if (tmsg == null) {
				if (msg != null && msg.startsWith("VDL2: ")) {
					tmsg = ex.getMessage().substring(6);
				}
				else {
					tmsg = ex.toString();
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
		if (count.size() != 0) {
			STDERR.ret(stack, "The following errors have occurred:\n");
			i = count.entrySet().iterator();
			int k = 1;
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Integer j = (Integer) e.getValue();
				if (j.intValue() == 1) {
					STDERR.ret(stack, k + ". " + e.getKey() + "\n");
				}
				else {
					STDERR.ret(stack, k + ". " + e.getKey() + " (" + j.intValue() + " times)\n");
				}
				k++;
			}
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}
}
