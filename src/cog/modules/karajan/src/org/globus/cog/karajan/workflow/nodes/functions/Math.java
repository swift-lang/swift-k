// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.NumericEqualityComparator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Math extends FunctionsCollection {

	static {
		setArguments("math_lessorequal", ARGS_2VALUES);
		addAlias("math___lt___eq_", "math_lessorequal");
	}

	public boolean math_lessorequal(VariableStack stack) throws ExecutionException {
		;
		Number[] v = getArgs(stack);
		return v[0].doubleValue() <= v[1].doubleValue();
	}

	static {
		setArguments("math_greaterorequal", ARGS_2VALUES);
		addAlias("math___gt___eq_", "math_greaterorequal");
	}

	public boolean math_greaterorequal(VariableStack stack) throws ExecutionException {
		Number[] v = getArgs(stack);
		return v[0].doubleValue() >= v[1].doubleValue();
	}

	static {
		setArguments("math_lessthan", ARGS_2VALUES);
		addAlias("math___lt_", "math_lessthan");
	}

	public boolean math_lessthan(VariableStack stack) throws ExecutionException {
		Number[] v = getArgs(stack);
		return v[0].doubleValue() < v[1].doubleValue();
	}

	static {
		setArguments("math_greaterthan", ARGS_2VALUES);
		addAlias("math___gt_", "math_greaterthan");
	}

	public boolean math_greaterthan(VariableStack stack) throws ExecutionException {
		Number[] v = getArgs(stack);
		return v[0].doubleValue() > v[1].doubleValue();
	}

	static {
		setArguments("math_equalsnumeric", ARGS_2VALUES);
	}

	public boolean math_equalsnumeric(VariableStack stack) throws ExecutionException {
		Object[] args = getArguments(ARGS_2VALUES, stack);
		return new NumericEqualityComparator().equals(args[0], args[1]);
	}

	static {
		setArguments("math_product", new Arg[] { Arg.VARGS });
		addAlias("math___times_", "math_product");
	}

	public double math_product(VariableStack stack) throws ExecutionException {
		double product = 1;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			product = product * TypeUtil.toDouble(args[i]);
		}
		return product;
	}
	
	public static final Arg A_DIVIDEND = new Arg.Positional("dividend");
	public static final Arg A_DIVISOR = new Arg.Positional("divisor");

	static {
		setArguments("math_quotient", new Arg[] { A_DIVIDEND, A_DIVISOR });
		addAlias("math___fwslash_", "math_quotient");
	}

	public double math_quotient(VariableStack stack) throws ExecutionException {
		try {
			return TypeUtil.toDouble(A_DIVIDEND.getValue(stack))
					/ TypeUtil.toDouble(A_DIVISOR.getValue(stack));
		}
		catch (ArithmeticException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
	}

	static {
		setArguments("math_remainder", new Arg[] { A_DIVIDEND, A_DIVISOR });
		addAlias("math___percent_", "math_remainder");
	}

	public double math_remainder(VariableStack stack) throws ExecutionException {
		try {
			return TypeUtil.toDouble(A_DIVIDEND.getValue(stack))
					% TypeUtil.toDouble(A_DIVISOR.getValue(stack));
		}
		catch (ArithmeticException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
	}
	
	public static final Arg A_VALUE = new Arg.Positional("value");

	static {
		setArguments("math_square", new Arg[] { A_VALUE });
	}

	public double math_square(VariableStack stack) throws ExecutionException {
		double val = TypeUtil.toDouble(A_VALUE.getValue(stack));
		return val * val;
	}

	static {
		setArguments("math_sqrt", new Arg[] { A_VALUE });
	}

	public double math_sqrt(VariableStack stack) throws ExecutionException {
		double val = TypeUtil.toDouble(A_VALUE.getValue(stack));
		return java.lang.Math.sqrt(val);
	}
	
	public static final Arg A_FROM = new Arg.Positional("from");

	static {
		setArguments("math_subtraction", new Arg[] { A_FROM, A_VALUE });
		addAlias("math___minus_", "math_subtraction");
	}

	public double math_subtraction(VariableStack stack) throws ExecutionException {
		return TypeUtil.toDouble(A_FROM.getValue(stack))
				- TypeUtil.toDouble(A_VALUE.getValue(stack));
	}

	static {
		setArguments("math_sum", new Arg[] { Arg.VARGS });
		addAlias("math___plus_", "math_sum");
	}

	public double math_sum(VariableStack stack) throws ExecutionException {
		double sum = 0;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			sum += TypeUtil.toDouble(args[i]);
		}
		return sum;
	}

	static {
		setArguments("math_min", new Arg[] { Arg.VARGS });
	}

	public double math_min(VariableStack stack) throws ExecutionException {
		double min = Double.MAX_VALUE;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			double tmp = TypeUtil.toDouble(args[i]);
			if (min > tmp) {
				min = tmp;
			}
		}
		return min;
	}

	static {
		setArguments("math_max", new Arg[] { Arg.VARGS });
	}

	public double math_max(VariableStack stack) throws ExecutionException {
		double max = Double.MIN_VALUE;
		Object[] args = Arg.VARGS.asArray(stack);
		for (int i = 0; i < args.length; i++) {
			double tmp = TypeUtil.toDouble(args[i]);
			if (max < tmp) {
				max = tmp;
			}
		}
		return max;
	}

	static {
		setArguments("math_int", new Arg[] { A_VALUE });
	}

	public double math_int(VariableStack stack) throws ExecutionException {
		return java.lang.Math.floor(TypeUtil.toDouble(A_VALUE.getValue(stack)));
	}

	static {
		setArguments("math_ln", new Arg[] { A_VALUE });
	}

	public double math_ln(VariableStack stack) throws ExecutionException {
		return java.lang.Math.log(TypeUtil.toDouble(A_VALUE.getValue(stack)));
	}
	
	static {
		setArguments("math_exp", new Arg[] { A_VALUE });
	}

	public double math_exp(VariableStack stack) throws ExecutionException {
		return java.lang.Math.exp(TypeUtil.toDouble(A_VALUE.getValue(stack)));
	}


	public double math_random(VariableStack stack) throws ExecutionException {
		return java.lang.Math.random();
	}

	
	public double math_nan(VariableStack stack) {
		return Double.NaN;
	}
}
