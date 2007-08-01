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

	public static final SwiftArg L = new SwiftArg.Positional("left");
	public static final SwiftArg R = new SwiftArg.Positional("right");

	public static final String FLOAT = "float";
	public static final String INT = "int";
	public static final String BOOLEAN = "boolean";

	private DSHandle newNum(String type, double value) throws ExecutionException {
		try {
			DSHandle handle = new RootDataNode(type);
			handle.setValue(new Double(value));
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

	private String type(VariableStack stack) throws ExecutionException {
		if (FLOAT.equals(L.getType(stack)) || FLOAT.equals(R.getType(stack))) {
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
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(type(stack), l + r);
	}

	public Object vdlop_subtraction(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(type(stack), l - r);
	}

	public Object vdlop_product(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(type(stack), l * r);
	}

	public Object vdlop_quotient(VariableStack stack) throws ExecutionException {
		// for now we map to this one
		return vdlop_fquotient(stack);
	}

	public Object vdlop_fquotient(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(FLOAT, l / r);
	}

	public Object vdlop_iquotient(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(INT, l / r);
	}

	public Object vdlop_remainder(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(type(stack), l % r);
	}

	public Object vdlop_le(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newBool(l <= r);
	}

	public Object vdlop_ge(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newBool(l >= r);
	}

	public Object vdlop_gt(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newBool(l > r);
	}

	public Object vdlop_lt(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newBool(l < r);
	}

	public Object vdlop_eq(VariableStack stack) throws ExecutionException {
		Object l = L.getValue(stack);
		Object r = R.getValue(stack);
		if (l == null) {
			throw new ExecutionException("First operand is null");
		}
		if (r == null) {
			throw new ExecutionException("Second operand is null");
		}
		return newBool(l.equals(r));
	}
}
