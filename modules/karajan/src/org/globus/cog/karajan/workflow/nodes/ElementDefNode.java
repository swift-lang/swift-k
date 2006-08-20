// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.NonCacheable;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.JavaElement;

public class ElementDefNode extends SequentialWithArguments implements NonCacheable {
	private static final Logger logger = Logger.getLogger(ElementDefNode.class);

	public static final Arg A_TYPE = new Arg.Positional("type", 0);
	public static final Arg A_CLASSNAME = new Arg.Optional("classname");

	static {
		setArguments(ElementDefNode.class, new Arg[] { A_TYPE, A_CLASSNAME });
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar("#quoted", true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		boolean trusted;
		try {
			trusted = stack.getBooleanVar("#trusted");
		}
		catch (VariableNotFoundException e) {
			trusted = true;
		}
		if (!trusted) {
			stack.dumpAll();
			throw new ExecutionException("The use of " + getElementType()
					+ " is not allowed in a restricted environment");
		}
		String prefix = TypeUtil.toString(stack.getVar("#namespaceprefix"));
		if (A_TYPE.isPresent(stack)) {
			DefUtil.addDef(stack, stack.parentFrame(), prefix, TypeUtil.toIdentifier(
					A_TYPE.getValue(stack)).getName(), new JavaElement(
					TypeUtil.toString(A_CLASSNAME.getValue(stack))));
		}
		else {
			ret(stack, new JavaElement(TypeUtil.toString(A_CLASSNAME.getValue(stack))));
		}
		super.post(stack);
	}
}