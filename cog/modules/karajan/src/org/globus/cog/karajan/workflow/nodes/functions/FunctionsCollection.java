// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;

public abstract class FunctionsCollection extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(FunctionsCollection.class);
	
	public static final Arg PA_VALUE1 = new Arg.Positional("value1");
	public static final Arg PA_VALUE2 = new Arg.Positional("value2");
	
	public static final Arg[] ARGS_2VALUES = new Arg[] { PA_VALUE1, PA_VALUE2 };

	private static final Class[] sig = new Class[] { VariableStack.class };
	private Method method;

	private static Set inlineText = new HashSet();

	protected static void setAcceptsInlineText(String fname, boolean text) {
		if (!text) {
			inlineText.add(fname);
		}
		else {
			inlineText.remove(fname);
		}
	}

	protected static void addAlias(String from, String to) {
		Aliases.add(from, to);
	}

	public void setElementType(String type) {
		super.setElementType(type);
		String methodName = type.replaceAll(":", "_").toLowerCase();
		String alias = Aliases.getAlias(methodName);
		if (alias != null) {
			methodName = alias;
		}
		try {
			method = this.getClass().getMethod(methodName, sig);
		}
		catch (NoSuchMethodException e) {
			try {
				method = this.getClass().getMethod("_" + methodName, sig);
			}
			catch (NoSuchMethodException ee) {
				throw new KarajanRuntimeException("No method found for function "
						+ getElementType());
			}
		}
	}

	public String getCanonicalName() {
		return method.getName();
	}

	public Object function(VariableStack stack) throws ExecutionException {
		try {
			return method.invoke(this, new Object[] { stack });
		}
		catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof ExecutionException) {
				throw (ExecutionException) e.getTargetException();
			}
			else if (e.getTargetException() instanceof FutureNotYetAvailable) {
				throw (FutureNotYetAvailable) e.getTargetException();
			}
			throw new ExecutionException(e.getTargetException());
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	public long currenttime(VariableStack stack) throws ExecutionException {
		return System.currentTimeMillis();
	}

	public Number[] getArgs(VariableStack stack) throws ExecutionException {
		Object[] args = getArguments(ARGS_2VALUES, stack);
		Number a1 = TypeUtil.toNumber(args[0]);
		Number a2 = TypeUtil.toNumber(args[1]);
		return new Number[] { a1, a2 };
	}

	public boolean acceptsInlineText() {
		return !inlineText.contains(method.getName());
	}

	public Object getCanonicalType() {
		return method.getName();
	}

	private static Set quotedArgs = new HashSet();

	protected static void setQuotedArgs(String fn) {
		quotedArgs.add(fn);
	}

	protected void initializeStatic() {
		super.initializeStatic();
		if (quotedArgs.contains(getCanonicalType())) {
			this.setQuotedArgs(true);
		}
	}
}