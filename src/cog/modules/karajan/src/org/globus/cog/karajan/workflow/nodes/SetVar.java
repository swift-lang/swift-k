// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ElementProperty;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class SetVar extends SequentialWithArguments {
	public static final Arg A_NAME = new Arg.Optional("name");
	public static final Arg A_NAMES = new Arg.Optional("names");
	public static final Arg A_VALUE = new Arg.Optional("value");

	private Object value;
	private Object[] names;

	static {
		setArguments(SetVar.class, new Arg[] { A_NAME, A_NAMES, A_VALUE, Arg.VARGS });
	}

	public void post(VariableStack stack) throws ExecutionException {
		Object[] vargs = Arg.VARGS.asArray(stack);
		if (A_VALUE.isPresent(stack)) {
			if (vargs.length != 0) {
				throw new ExecutionException(
						"Variable arguments and the 'value' attribute must be used exclusively");
			}
			vargs = new Object[] { A_VALUE.getValue(stack) };
		}
		if (names.length != vargs.length) {
			throw new ExecutionException(names.length + " names specified; " + vargs.length
					+ " arguments found");
		}
		for (int i = 0; i < vargs.length; i++) {
			String name;
			if (names[i] instanceof String) {
				name = (String) names[i];
			}
			else if (names[i] instanceof ElementProperty) {
				name = TypeUtil.toString(((ElementProperty) names[i]).getValue(stack)).toLowerCase();
			}
			else {
				name = TypeUtil.toString(names[i]);
			}
			if (name.startsWith("#")) {
				throw new ExecutionException("Invalid character ('#') in identifier: " + name);
			}
			getFrame(stack).setVar(name, vargs[i]);
		}
		super.post(stack);
	}

	protected StackFrame getFrame(VariableStack stack) {
		return stack.parentFrame();
	}

	protected void initializeStatic() throws KarajanRuntimeException {
		super.initializeStatic();
		if (A_NAME.isPresentStatic(this)) {
			names = new Object[] { A_NAME.getStatic(this) };
			if (names[0] instanceof String) {
				names[0] = ((String) names[0]).toLowerCase();
			}
		}
		else if (A_NAMES.isPresentStatic(this)) {
			names = TypeUtil.toLowerStringArray((String) A_NAMES.getStatic(this));
			if (names.length == 0) {
				throw new KarajanRuntimeException("'names' attribute is empty");
			}
		}
		else {
			throw new KarajanRuntimeException("No 'name' or 'names' attribute specified");
		}
		if (A_VALUE.isPresentStatic(this)) {
			value = A_VALUE.getStatic(this);
		}
	}
}