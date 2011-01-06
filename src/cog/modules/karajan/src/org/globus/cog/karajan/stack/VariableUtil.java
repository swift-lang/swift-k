// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.stack;

import org.globus.cog.karajan.util.ElementProperty;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public final class VariableUtil {

	public static Object expandProperty(final FlowElement element, final VariableStack stack,
			final String name) throws ExecutionException {
		final Object prop = element.getProperty(name);
		if (prop == null && !element.hasProperty(name)) {
			throw new ExecutionException(stack, "Missing attribute: " + name);
		}
		return expand(prop, stack);
	}

	public static Object expand(final Object value, final VariableStack stack) throws ExecutionException {
		if (value == null) {
			return null;
		}
		if (stack == null) {
			return value;
		}
		else {
			if (value instanceof ElementProperty) {
				return ((ElementProperty) value).getValue(stack);
			}
			return value;
		}
	}

	/**
	 * @deprecated Forward arguments have been removed
	 */
	public static boolean isForwardArgument(String arg) {
		if (!arg.startsWith("{")) {
			return false;
		}
		if (!arg.endsWith("}")) {
			return false;
		}
		try {
			Integer.parseInt(arg.substring(1, arg.length() - 2));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * @deprecated Forward arguments have been removed
	 * Returns the index from a forward argument. A forward argument has the
	 * form {&lt;n&gt;}, where &lt;n&gt; is the index. Returns -1 if the
	 * argument is not a valid forward argument.
	 */
	public static int getForwardArgumentIndex(String arg) {
		if (!arg.startsWith("{")) {
			return -1;
		}
		if (!arg.endsWith("}")) {
			return -1;
		}
		try {
			return Integer.parseInt(arg.substring(1, arg.length() - 2));
		}
		catch (Exception e) {
			return -1;
		}
	}

	public static void printKarajanStackTrace(VariableStack stack) {

	}
}