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
import org.globus.cog.karajan.translator.atoms.Transliterator;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.JavaElement;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;

public class Define extends SequentialWithArguments {
	public static final Arg A_NAME = new Arg.Positional("name", 0);
	public static final Arg A_VALUE = new Arg.Positional("value", 1);
	
	private static Definer definer = new Definer();

	static {
		setArguments(Define.class, new Arg[] { A_NAME, A_VALUE });
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar("#quoted", true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		String name = TypeUtil.toIdentifier(A_NAME.getValue(stack)).getName();
		Object value = A_VALUE.getValue(stack);
		String nsprefix = TypeUtil.toString(stack.getVar("#namespaceprefix"));
		getDefiner().define(stack, name, nsprefix, value);
		super.post(stack);
	}

	protected StackFrame getFrame(VariableStack stack) {
		return stack.parentFrame();
	}

	protected Definer getDefiner() {
		return definer;
	}

	public static class Definer {
		protected void define(VariableStack stack, String name, String nsprefix, Object def)
				throws ExecutionException {
			if (def instanceof JavaElement || def instanceof UDEDefinition) {
				DefUtil.addDef(stack, stack.parentFrame(), nsprefix,
					Transliterator.transliterate(name), def);
			}
			else {
				throw new ExecutionException("Cannot define a non-lambda");
			}
		}
	}
}