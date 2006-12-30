/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;

public class Range extends VDLFunction {
	public static final Arg PA_FROM = new Arg.Positional("from");
	public static final Arg PA_TO = new Arg.Positional("to");
	public static final Arg OA_STEP = new Arg.Optional("step");

	static {
		setArguments(Range.class, new Arg[] { PA_FROM, PA_TO, OA_STEP });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		// TODO: deal with expression
		Object from = PA_FROM.getValue(stack);
		Object to = PA_TO.getValue(stack);
		Object step = OA_STEP.getValue(stack);

		String type = "";
		Object start, stop, incr;
		if (from instanceof DSHandle) {
			type = ((DSHandle) from).getType();
			start = ((DSHandle) from).getValue();
		}
		else {
			type = inferType(from);
			start = from;
		}

		if (to instanceof DSHandle) {
			if (!compatible(((DSHandle) to).getType(), type))
				throw new ExecutionException("Range: from and to type mismatch");
			stop = ((DSHandle) to).getValue();
		}
		else {
			if (!compatible(inferType(to), type))
				throw new ExecutionException("Range: from and to type mismatch");
			stop = to;
		}

		if (step != null) {
			if (step instanceof DSHandle) {
				if (!compatible(((DSHandle) step).getType(), type))
					throw new ExecutionException("Range: step type mismatch");
				incr = ((DSHandle) step).getValue();
			}
			else {
				if (!compatible(inferType(step), type))
					throw new ExecutionException("Range: step type mismatch");
				incr = step;
			}
		}
		else {
			incr = new Integer(1);
		}
		// only deal with int and float
		try {
			AbstractDataNode handle;
			if (type.equals("int")) {
				handle = new RootArrayDataNode(type);
				int s = Integer.parseInt(start.toString());
				int t = Integer.parseInt(stop.toString());
				int p = Integer.parseInt(incr.toString());
				int index = 0;
				for (int v = s; v <= t; v += p, index++) {
					Path path = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					DSHandle field = handle.getField(path);
					field.setValue(new Integer(v));
					closeShallow(stack, field);
				}
			}
			else if (type.equals("float")) {
				handle = new RootArrayDataNode(type);
				double s = Double.parseDouble(start.toString());
				double t = Double.parseDouble(stop.toString());
				double p = Double.parseDouble(incr.toString());
				int index = 0;
				for (double v = s; v <= t; v += p, index++) {
					Path path = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					DSHandle field = handle.getField(path);
					field.setValue(new Double(v));
					closeShallow(stack, field);
				}
			}
			else
				return null;
			closeShallow(stack, handle);
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}

	}
}
