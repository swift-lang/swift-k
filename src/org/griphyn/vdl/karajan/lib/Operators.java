package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;

public class Operators extends FunctionsCollection {
	private static final Logger logger = Logger.getLogger(Operators.class);

	public static final Arg L = new Arg.Positional("left");
	public static final Arg R = new Arg.Positional("right");

	public static final String FLOAT = "float";
	public static final String INT = "int";
	public static final String BOOLEAN = "boolean";

	private DSHandle getArg(final Arg arg, final VariableStack stack) throws ExecutionException {
		Object value = arg.getValue(stack);
		try {
			return (DSHandle) value;
		}
		catch (ClassCastException e) {
			if (value == null) {
				throw new ExecutionException("Null argument supplied to Swift operator");
			}
			else {
				throw new ExecutionException("Invalid argument type supplied to Swift operator ("
						+ value.getClass() + ")");
			}
		}
	}

	private DSHandle newNum(String type, double value) throws ExecutionException {
		try {
			DSHandle handle = new RootDataNode(type);
			if (INT.equals(type)) {
				handle.setValue(new Integer((int) value));
			}
			else {
				handle.setValue(new Double(value));
			}
			handle.closeShallow();
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException("Internal error", e);
		}
	}

	private DSHandle newBool(boolean value) throws ExecutionException {
		try {
			DSHandle handle = new RootDataNode(BOOLEAN);
			handle.setValue(new Boolean(value));
			handle.closeShallow();
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException("Internal error", e);
		}
	}

	private double value(DSHandle handle) throws ExecutionException {
		String type = handle.getType();
		if (INT.equals(type) || FLOAT.equals(type)) {
			try {
				return ((Number) handle.getValue()).doubleValue();
			}
			catch (ClassCastException e) {
				throw new ExecutionException("Handle says it's a numeric type, but it holds a "
						+ handle.getValue().getClass());
			}
		}
		else {
			throw new ExecutionException(
					"Type error. Numeric operator used with a non-numeric operand (" + type + ")");
		}
	}

	private String type(DSHandle l, DSHandle r) {
		if (FLOAT.equals(l.getType()) || FLOAT.equals(r.getType())) {
			return FLOAT;
		}
		else {
			return INT;
		}
	}

	private static final String[] OPERATORS = new String[] { "vdlop_sum", "vdlop_subtraction",
			"vdlop_product", "vdlop_quotient", "vdlop_fquotient", "vdlop_iquotient",
			"vdlop_remainder", "vdlop_le", "vdlop_ge", "vdlop_lt", "vdlop_gt", "vdlop_eq" };

	private static final Arg[] OPARGS = new Arg[] { L, R };

	static {
		for (int i = 0; i < OPERATORS.length; i++) {
			setArguments(OPERATORS[i], OPARGS);
		}
	}

	public Object vdlop_sum(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(type(l, r), value(l) + value(r));
	}

	public Object vdlop_subtraction(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(type(l, r), value(l) - value(r));
	}

	public Object vdlop_product(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(type(l, r), value(l) * value(r));
	}

	public Object vdlop_quotient(VariableStack stack) throws ExecutionException {
		// for now we map to this one
		return vdlop_fquotient(stack);
	}

	public Object vdlop_fquotient(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(FLOAT, value(l) / value(r));
	}

	public Object vdlop_iquotient(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(INT, value(l) / value(r));
	}

	public Object vdlop_remainder(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newNum(type(l, r), value(l) % value(r));
	}

	public Object vdlop_le(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newBool(value(l) <= value(r));
	}

	public Object vdlop_ge(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newBool(value(l) >= value(r));
	}

	public Object vdlop_gt(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newBool(value(l) > value(r));
	}

	public Object vdlop_lt(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newBool(value(l) < value(r));
	}

	public Object vdlop_eq(VariableStack stack) throws ExecutionException {
		DSHandle l = getArg(L, stack);
		DSHandle r = getArg(R, stack);
		return newBool(value(l) == value(r));
	}
}
