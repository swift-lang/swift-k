/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;

public class UnwrapClosedList extends VDLFunction {
	public static final Logger logger = Logger.getLogger(UnwrapClosedList.class);
	
	public static final Arg.Positional PA_LIST = new Arg.Positional("list");

	static {
		setArguments(UnwrapClosedList.class, new Arg[] { PA_LIST });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		@SuppressWarnings("unchecked")
        List<DSHandle> l = (List<DSHandle>) PA_LIST.getValue(stack);
		
		List<Object> r = new ArrayList<Object>(l.size());
		
		for (DSHandle h : l) {
		    if (h.getType().isArray()) {
		        Map<?, DSHandle> m = h.getArrayValue();
		        for (DSHandle h2 : m.values()) {
		            r.add(h2.getValue());
		        }
		    }
		    else {
		        r.add(h.getValue());
		    }
		}
		
		return r;
	}
}
