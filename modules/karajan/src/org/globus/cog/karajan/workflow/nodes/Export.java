//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Collection;
import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.ElementDefinition;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Export extends Define {
	public static final Arg A_RESTRICTED = new Arg.Optional("restricted", Boolean.FALSE);
	public static final Arg.Channel DEF_CHANNEL = new Arg.Channel("defs");

	private static Definer definer = new Definer();

	static {
		setArguments(Export.class, new Arg[] { A_NAME, A_VALUE, A_RESTRICTED });
	}

	protected Define.Definer getDefiner() {
		return definer;
	}

	public static class Definer extends Define.Definer {
		protected void define(VariableStack stack, String name, String nsprefix, Object def)
				throws ExecutionException {
			DEF_CHANNEL.ret(stack, new ElementDefinition(nsprefix, name, def, isRestricted(stack)));
			super.define(stack, name, nsprefix, def);
		}

		private boolean isRestricted(VariableStack stack) {
			try {
				return TypeUtil.toBoolean(A_RESTRICTED.getValue(stack));
			}
			catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void post(VariableStack stack) throws ExecutionException {
		if (!A_NAME.isPresent(stack)) {
			/*
			 * A hack to simplify porting old code All definitions on current
			 * frame are exported
			 */
			// TODO Deal with cases when a deflist also contains things with the
			// same name but different prefix.
			Collection names = stack.currentFrame().names();
			Iterator i = names.iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				Object value = stack.currentFrame().getVar(name);
				if (value instanceof DefList) {
					DefList defList = (DefList) value;
					String[] prefixes = defList.currentPrefixes();
					for (int j = 0; j < prefixes.length; j++) {
						String nsprefix = prefixes[j];
						DEF_CHANNEL.ret(stack, new ElementDefinition(nsprefix, defList.getName(),
								defList.get(nsprefix), false));
					}
				}
			}
			complete(stack);
		}
		else {
			super.post(stack);
		}
	}
}
