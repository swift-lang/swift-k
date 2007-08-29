/*
 * Created on Sep 28, 2006
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;
import org.griphyn.vdl.karajan.lib.SwiftArg;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

public class FnArg extends AbstractFunction {
	public static final String PARSED_ARGS = "cmdline:named";

	public static final SwiftArg P_NAME = new SwiftArg.Positional("name");

	static {
		setArguments(FnArg.class, new Arg[] { P_NAME });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		synchronized (stack.firstFrame()) {
			if (!stack.firstFrame().isDefined(PARSED_ARGS)) {
				List argv = (List) stack.firstFrame().getVar(ExecutionContext.CMDLINE_ARGS);
				Map named = new HashMap();
				Iterator i = argv.iterator();
				while (i.hasNext()) {
					String arg = (String) i.next();
					if (!arg.startsWith("-")) {
						continue;
					}
					int index = arg.indexOf('=');
					if (index == -1 || (arg.charAt(0) != '-')) {
						throw new ExecutionException("Invalid command line argument: " + arg);
					}
					else {
						String name = arg.substring(1, index);
						named.put(name, arg.substring(index + 1));
					}
				}
				stack.firstFrame().setVar(PARSED_ARGS, named);
			}
		}
		Map args = (Map) stack.firstFrame().getVar(PARSED_ARGS);
		String name = TypeUtil.toString(P_NAME.getValue(stack));
		name = name.trim();
		if (name.startsWith("\"") && name.endsWith("\"")) {
			name = name.substring(1, name.length() - 1);
		}
		Object value = args.get(name);
		if (value == null) {
			throw new ExecutionException("Missing command line argument: " + name);
		}
		else {
			return RootDataNode.newNode(Types.STRING, value);	
		}
	}
}
