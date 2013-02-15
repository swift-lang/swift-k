//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 16, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ArgumentsMap;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.JavaElement;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.nodes.user.UserDefinedElement;

public class Info extends AbstractSequentialWithArguments {
	public static final Arg A_PREFIX = new Arg.Optional("prefix");
	public static final Arg A_NAME = new Arg.Optional("name");
	
	static {
		setArguments(Info.class, new Arg[] { A_NAME, A_PREFIX });
	}

	public Info() {
		setQuotedArgs(true);
	}

	protected void argumentsEvaluated(VariableStack stack) throws ExecutionException {
		VariableArguments stdout = STDOUT.getReturn(stack);
		if (A_PREFIX.isPresent(stack)) {
			String prefix = TypeUtil.toIdentifier(A_PREFIX.getValue(stack)).toString();
			find(prefix, stack, stdout);
		}
		else if (A_NAME.isPresent(stack)) {
			String name = TypeUtil.toIdentifier(A_NAME.getValue(stack)).toString();
			printDef(name, DefUtil.getDef(stack, name, this).getDef(), stdout);
		}
		else {
			find(null, stack, stdout);
		}
	}

	protected void find(String prefix, VariableStack stack, VariableArguments stdout)
			throws ExecutionException {
		for (int i = 0; i < stack.frameCount(); i++) {
			StackFrame frame = stack.getFrame(i);
			Iterator n = frame.names().iterator();
			while (n.hasNext()) {
				String name = (String) n.next();
				if (name.startsWith("#def#")) {
					DefList dl = (DefList) frame.getVar(name);
					String elName = name.substring(5);
					if (prefix != null) {
						if (dl.contains(prefix)) {
							Object def = dl.get(prefix);
							printDef(prefix + ":" + elName, def, stdout);
						}
					}
					else {
						Iterator mi = dl.prefixes().iterator();
						while (mi.hasNext()) {
							String p = (String) mi.next();
							printDef(p + ":" + elName, dl.get(p), stdout);
						}
					}
				}
			}
		}
	}

	protected void printDef(String name, Object def, VariableArguments stdout) {
		stdout.append(ppDef(name, def) + "\n");
	}

	public static String ppDef(String name, Object def) {
		if (def instanceof JavaElement) {
			return ppDef(name, (JavaElement) def);
		}
		else if (def instanceof UserDefinedElement) {
			return ppDef(name, (UserDefinedElement) def);
		}
		else if (def instanceof UDEDefinition) {
			return ppDef(name, ((UDEDefinition) def).getUde());
		}
		else {
			return name + ": " + def.getClass();
		}
	}

	public static String ppDef(String name, JavaElement je) {
		Class cls = je.getElementClass();
		Object internalId;
		if (FunctionsCollection.class.isAssignableFrom(cls)) {
			FlowNode fe = (FlowNode) je.newInstance();
			fe.setElementType(name);
			internalId = fe.getCanonicalType();
		}
		else {
			internalId = cls;
		}
		return ppDef(name, internalId);
	}
	
	public static String ppDef(String name, FlowNode fe) {
		return ppDef(name, fe.getCanonicalType());
	}
	
	protected static String ppDef(String name, String internalId) {
		String args = "()";
		Map valid = (Map) ArgumentsMap.getMap().getValidArgs().get(internalId);
		boolean vargs = ArgumentsMap.getMap().getVargs().contains(internalId);
		if (valid != null && valid.size() > 0) {
			String[] items = new String[vargs ? valid.size() + 1 : valid.size()];
			Iterator v = valid.entrySet().iterator();
			int opti = items.length;
			while (v.hasNext()) {
				Map.Entry entry = (Map.Entry) v.next();
				int index = ((Integer) entry.getValue()).intValue();
				if (index == Arg.NOINDEX) {
					items[--opti] = entry.getKey() + "*";
				}
				else {
					items[index] = (String) entry.getKey();
				}
			}
			if (vargs) {
				items[--opti] = "...";
			}
			StringBuffer sb = new StringBuffer();
			sb.append('(');
			for (int j = 0; j < items.length - 1; j++) {
				sb.append(items[j]);
				sb.append(", ");
			}
			sb.append(items[items.length - 1]);
			sb.append(')');
			args = sb.toString();
		}
		return demangle(name) + args;
	}

	public static String ppDef(String name, UserDefinedElement def) {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (int i = 0; i < def.getArguments().length; i++) {
			appendArg(sb, def.getArguments()[i]);
		}
		if (def.hasVargs()) {
			appendArg(sb, "...");
		}
		for (int i = 0; i < def.getOptargs().length; i++) {
			appendArg(sb, def.getOptargs()[i] + "*");
		}
		sb.append(')');
		return demangle(name) + sb.toString();
	}

	private static void appendArg(StringBuffer sb, String arg) {
		if (sb.length() != 1) {
			sb.append(", ");
		}
		sb.append(arg);
	}

	private static String demangle(String name) {
		if (name.indexOf("__") != -1) {
			name = name.replaceAll("__q_", "?");
			name = name.replaceAll("__bang_", "!");
			name = name.replaceAll("__eq_", "=");
			name = name.replaceAll("__amp_", "&");
			name = name.replaceAll("__dot_", ".");
			name = name.replaceAll("__pipe_", "|");
			name = name.replaceAll("__lt_", "<");
			name = name.replaceAll("__gt_", ">");
			name = name.replaceAll("__times_", "*");
			name = name.replaceAll("__fwslash_", "/");
			name = name.replaceAll("__percent_", "%");
			name = name.replaceAll("__minus_", "-");
			name = name.replaceAll("__plus_", "+");
			return name;
		}
		else {
			return name;
		}
	}
}
