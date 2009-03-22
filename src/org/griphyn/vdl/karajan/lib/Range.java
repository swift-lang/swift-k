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
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Range extends VDLFunction {
	public static final SwiftArg PA_FROM = new SwiftArg.Positional("from");
	public static final SwiftArg PA_TO = new SwiftArg.Positional("to");
	public static final SwiftArg OA_STEP = new SwiftArg.Optional("step", new Double(1), Types.FLOAT);

	static {
		setArguments(Range.class, new Arg[] { PA_FROM, PA_TO, OA_STEP });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		// TODO: deal with expression
		Type type = PA_FROM.getType(stack);
		double start = PA_FROM.getDoubleValue(stack);
		double stop = PA_TO.getDoubleValue(stack);
		double incr = OA_STEP.getDoubleValue(stack);

		// only deal with int and float
		try {
			AbstractDataNode handle;

			handle = new RootArrayDataNode(type.arrayType());
			int index = 0;
			for (double v = start; v <= stop; v += incr, index++) {
				Path path = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
				DSHandle field = handle.getField(path);
				field.setValue(new Double(v));
				closeShallow(stack, field);
			}

			closeShallow(stack, handle);
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}
