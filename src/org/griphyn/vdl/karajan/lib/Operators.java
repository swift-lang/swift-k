package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Operators extends FunctionsCollection {

	public static final SwiftArg L = new SwiftArg.Positional("left");
	public static final SwiftArg R = new SwiftArg.Positional("right");
	public static final SwiftArg U = new SwiftArg.Positional("unaryArg");

	private DSHandle newNum(Type type, double value) throws ExecutionException {
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
	
	private DSHandle newString(String value) throws ExecutionException {
		try {
			DSHandle handle = new RootDataNode(Types.STRING);
			handle.setValue(value);
			handle.closeShallow();
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException("Internal error", e);
		}
	}

	private DSHandle newBool(boolean value) throws ExecutionException {
		try {
			DSHandle handle = new RootDataNode(Types.BOOLEAN);
			handle.setValue(new Boolean(value));
			handle.closeShallow();
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException("Internal error", e);
		}
	}

	private Type type(VariableStack stack) throws ExecutionException {
		if (Types.FLOAT.equals(L.getType(stack)) || Types.FLOAT.equals(R.getType(stack))) {
			return Types.FLOAT;
		}
		else {
			return Types.INT;
		}
	}

	private static final String[] BINARY_OPERATORS = new String[] { "vdlop_sum", "vdlop_subtraction",
			"vdlop_product", "vdlop_quotient", "vdlop_fquotient", "vdlop_iquotient",
			"vdlop_remainder", "vdlop_le", "vdlop_ge", "vdlop_lt", "vdlop_gt", "vdlop_eq", "vdlop_ne", "vdlop_and", "vdlop_or" };
	private static final String[] UNARY_OPERATORS = new String[] { "vdlop_not" };

	private static final Arg[] BINARY_ARGS = new Arg[] { L, R };
	private static final Arg[] UNARY_ARGS = new Arg[] { U };

	static {
		for (int i = 0; i < BINARY_OPERATORS.length; i++) {
			setArguments(BINARY_OPERATORS[i], BINARY_ARGS);
		}
		for (int i = 0; i < UNARY_OPERATORS.length; i++) {
			setArguments(UNARY_OPERATORS[i], UNARY_ARGS);
		}
	}

	public Object vdlop_sum(VariableStack stack) throws ExecutionException {
		Object l = L.getValue(stack);
		Object r = R.getValue(stack);
		if (l instanceof String || r instanceof String) {
			return newString(((String) l) + ((String) r));
		}
		else {
			return newNum(type(stack), SwiftArg.checkDouble(l) + SwiftArg.checkDouble(r));
		}
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
		return newNum(Types.FLOAT, l / r);
	}

	public Object vdlop_iquotient(VariableStack stack) throws ExecutionException {
		double l = L.getDoubleValue(stack);
		double r = R.getDoubleValue(stack);
		return newNum(Types.INT, l / r);
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

	public Object vdlop_ne(VariableStack stack) throws ExecutionException {
		Object l = L.getValue(stack);
		Object r = R.getValue(stack);
		if (l == null) {
			throw new ExecutionException("First operand is null");
		}
		if (r == null) {
			throw new ExecutionException("Second operand is null");
		}
		return newBool(!(l.equals(r)));
	}

	public Object vdlop_and(VariableStack stack) throws ExecutionException {
		boolean l = ((Boolean)L.getValue(stack)).booleanValue();
		boolean r = ((Boolean)R.getValue(stack)).booleanValue();
		return newBool(l && r);
	}

	public Object vdlop_or(VariableStack stack) throws ExecutionException {
		boolean l = ((Boolean)L.getValue(stack)).booleanValue();
		boolean r = ((Boolean)R.getValue(stack)).booleanValue();
		return newBool(l || r);
	}

	public Object vdlop_not(VariableStack stack) throws ExecutionException {
		boolean u = ((Boolean)U.getValue(stack)).booleanValue();
		return newBool(!u);
	}
}
