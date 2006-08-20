// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class NewJavaObjectNode extends AbstractFunction {
    public static final Arg A_CLASSNAME = new Arg.Positional("classname", 0);
    public static final Arg A_TYPES = new Arg.Optional("types");
    
	static {
		setArguments(NewJavaObjectNode.class, new Arg[] { A_CLASSNAME, A_TYPES, Arg.VARGS });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String className = TypeUtil.toString(A_CLASSNAME.getValue(stack));
		Object[] args = Arg.VARGS.asArray(stack);
		Class[] argTypes = new Class[args.length];
		if (A_TYPES.isPresent(stack)) {
			List types = TypeUtil.toList(A_TYPES.getValue(stack));
			if (types.size() != args.length) {
				throw new ExecutionException(
						"The number of items in the types attribute does not match the number of arguments");
			}
			Iterator i = types.iterator();
			for (int j = 0; j < argTypes.length; j++) {
				String type = (String) i.next();
				argTypes[j] = JavaMethodInvocationNode.getClass(type);
				if (JavaMethodInvocationNode.TYPES.containsKey(type)) {
					args[j] = JavaMethodInvocationNode.convert(argTypes[j], args[j],
							argTypes[j].isArray());
				}
				else {
					if (args[j] != null) {
						if (!argTypes[j].isAssignableFrom(args[j].getClass())) {
							args[j] = JavaMethodInvocationNode.convert(argTypes[j], args[j],
									argTypes[j].isArray());
						}
					}
				}
			}
		}
		else {
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					argTypes[i] = null;
				}
				else {
					argTypes[i] = args[i].getClass();
				}
			}
		}
		try {
			Class c = Class.forName(className);
			return c.getConstructor(argTypes).newInstance(args);
		}
		catch (Exception e) {
			throw new ExecutionException("Could not instantiate " + className + " with arguments "
					+ JavaMethodInvocationNode.prettyPrintArray(args) + " ("
					+ e.getClass().getName() + ": " + e.getMessage() + ")", e);
		}
	}
}