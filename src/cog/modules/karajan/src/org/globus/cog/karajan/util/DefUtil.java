// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.util;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

public class DefUtil {
	public static final Logger logger = Logger.getLogger(DefUtil.class);
	public static final String ENV_CACHE = "##envcache";
	public static final String ENV = "#env";

	public static void addDef(VariableStack stack, StackFrame where, String prefix, String name,
			Object def) {
		String var = "#def#" + name.toLowerCase();
		DefList defs;
		if (stack.isDefined(var)) {
			try {
				defs = (DefList) stack.getVar(var);
			}
			catch (VariableNotFoundException e) {
				throw new KarajanRuntimeException(
						"Definition of element was removed from the stack. This shows some serious issue.");
			}
			defs = new DefList(defs);
			where.setVar(var, defs);
		}
		else {
			defs = new DefList(name);
			where.setVar(var, defs);
		}
		defs.put(prefix, def);
	}

	/**
	 * The environment cache is used to optimize the capturing of environments
	 * when defining elements
	 * 
	 * It is necessary because stack.copy() is a costly operation
	 * 
	 * This method does not initialize it, but it marks on a specific frame that
	 * any parent caches would be invalid. It is not initialized because there
	 * is no guarantee that an element would be defined in this environment,
	 * therefore the caching may be useless.
	 * 
	 * A lambda, if it finds an empty cache, should update it with a copy of the
	 * stack up to the frame in cause and from the closest barrier. It should
	 * also link with the previous environment.
	 */
	public static void updateEnvCache(VariableStack stack, StackFrame frame) {
		if (!frame.isDefined(ENV_CACHE)) {
			frame.setVar(ENV_CACHE, new DefinitionEnvironment());
		}
	}

	private static DefList.Entry search(VariableStack stack, String name, FlowElement parent)
			throws ExecutionException {
		String prefixed = name.toLowerCase();
		String unprefixed;
		String prefix;

		int delim = prefixed.indexOf(':');
		if (delim != -1) {
			prefix = prefixed.substring(0, delim);
			unprefixed = prefixed.substring(delim + 1);
		}
		else {
			unprefixed = prefixed;
			try {
				prefix = stack.getVarAsString("#namespaceprefix");
			}
			catch (VariableNotFoundException e) {
				if (parent != null) {
					prefix = (String) FlowNode.getTreeProperty("_namespaceprefix", parent);
				}
				else {
					prefix = "";
				}
			}
		}

		Object def = get(stack, prefix, unprefixed);
		if (def == null) {
			return getNoPrefix(stack, name);
		}
		else {
			return new DefList.Entry(unprefixed, prefix, def);
		}
	}

	private static DefList.Entry getNoPrefix(VariableStack stack, String name) throws ExecutionException {
		DefList prefixes = get(stack, name);
		if (prefixes == null) {
			return null;
		}
		checkAmbiguous(prefixes, name);
		return prefixes.first();
	}

	private static void checkAmbiguous(DefList prefixes, String name) {
		if (prefixes.size() > 1) {
			StringBuffer buf = new StringBuffer();
			buf.append("Ambiguous element: " + name + ". Possible choices:\n");
			Iterator i = prefixes.prefixes().iterator();
			while (i.hasNext()) {
				buf.append("\t" + i.next() + ":" + name + "\n");
			}
			throw new KarajanRuntimeException(buf.toString());
		}
	}

	private static Object get(VariableStack stack, String prefix, String name)
			throws ExecutionException {
		DefList prefixes = get(stack, name);
		if (prefixes == null) {
			return null;
		}
		return prefixes.get(prefix);
	}

	private static DefList get(VariableStack stack, final String name) throws ExecutionException {
		final String defName = "#def#" + name;
		try {
			return (DefList) stack.getShallowVar(defName);
		}
		catch (VariableNotFoundException f) {
			try {
				DefinitionEnvironment env = (DefinitionEnvironment) stack.getShallowVar(ENV);
				while (env != null) {
					try {
						return (DefList) env.getStack().getShallowVar(defName);
					}
					catch (VariableNotFoundException e) {
						env = env.getPrev();
					}
				}
				return null;
			}
			catch (VariableNotFoundException e) {
				return null;
			}
		}
	}

	public static DefList.Entry getDef(VariableStack stack, String type, FlowElement parent)
			throws ExecutionException {
		DefList.Entry value = search(stack, type, parent);
		if (value == null) {
			value = getNoPrefix(stack, "#");
		}
		return value;
	}
}
