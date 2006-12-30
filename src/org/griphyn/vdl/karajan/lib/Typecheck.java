/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;

public class Typecheck extends VDLFunction {
	public static final Arg PA_TYPE = new Arg.Positional("type");
	public static final Arg OA_ARGNAME = new Arg.Optional("argname");

	static {
		setArguments(Typecheck.class, new Arg[] { PA_VAR, PA_TYPE, OA_ISARRAY, OA_ARGNAME });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		String type = TypeUtil.toString(PA_TYPE.getValue(stack));
		boolean isArray = TypeUtil.toBoolean(OA_ISARRAY.getValue(stack));
		String argname = TypeUtil.toString(OA_ARGNAME.getValue(stack, null));
		if (!compatible(type, var.getType()) || !(isArray == var.isArray())) {
			if (argname != null) {
				throw new ExecutionException("Wrong type for argument '" + argname + "'. Expected "
						+ type + (isArray ? "[]" : "") + "; got " + var.getType()
						+ (var.isArray() ? "[]" : "") + ". Actual argument: " + var);
			}
			else {
				throw new ExecutionException("Wrong type for argument. Expected " + type
						+ (isArray ? "[]" : "") + "; got " + var.getType()
						+ (var.isArray() ? "[]" : "") + ". Actual argument: " + var);
			}
		}
		return null;
	}
}
