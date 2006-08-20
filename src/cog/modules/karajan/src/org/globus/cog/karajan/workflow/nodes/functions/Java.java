// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 15, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Java extends FunctionsCollection {
	public static final Arg PA_OBJECT = new Arg.Positional("object");
	
	static {
		setArguments("java_classof", new Arg[] { PA_OBJECT });
	}

	public String java_classof(VariableStack stack) throws ExecutionException {
		return PA_OBJECT.getValue(stack).getClass().getName();
	}
	
	public static final Arg PA_FIELD = new Arg.Positional("field");
	public static final Arg OA_OBJECT = new Arg.Optional("object", null);
	public static final Arg PA_CLASSNAME = new Arg.Positional("classname");

	static {
		setArguments("java_getfield", new Arg[] { PA_FIELD, OA_OBJECT, PA_CLASSNAME });
	}

	public Object java_getfield(VariableStack stack) throws ExecutionException {
		Object o = OA_OBJECT.getValue(stack);
		Class cls;
		if (o != null) {
			cls = o.getClass();
		}
		else {
			String className = TypeUtil.toString(PA_CLASSNAME.getValue(stack));
			try {
				cls = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				throw new ExecutionException("Invalid class name: " + className);
			}
		}
		try {
			return cls.getField(TypeUtil.toString(PA_FIELD.getValue(stack))).get(o);
		}
		catch (Exception e) {
			throw new ExecutionException("Could not get the specified field", e);
		}
	}
}