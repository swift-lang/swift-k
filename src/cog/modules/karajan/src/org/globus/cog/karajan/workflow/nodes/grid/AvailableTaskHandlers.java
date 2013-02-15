//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2007
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TaskHandlerWrapper;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class AvailableTaskHandlers extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(AvailableTaskHandlers.class);

	public static final Arg A_TYPE = new Arg.Optional("type");
	public static final Arg A_ALIASES = new Arg.Optional("includeAliases", Boolean.FALSE);

	static {
		setArguments(AvailableTaskHandlers.class, new Arg[] { A_TYPE, A_ALIASES });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		boolean aliases = TypeUtil.toBoolean(A_ALIASES.getValue(stack)); 
		if (A_TYPE.isPresent(stack)) {
			getHandlers(stack, TypeUtil.toString(A_TYPE.getValue(stack)), aliases);
		}
		else {
			getHandlers(stack, "execution", aliases);
			getHandlers(stack, "file", aliases);
			getHandlers(stack, "file-transfer", aliases);
		}
		return null;
	}

	protected void getHandlers(VariableStack stack, String type, boolean includeAliases) throws ExecutionException {
		String atype = TaskHandlerNode.karajanToAbstractionType(type);
		int itype = TaskHandlerNode.abstractionToHandlerType(atype);

		List<String> providers = AbstractionProperties.getProviders(atype);
		for (String provider : providers) {
			if (logger.isDebugEnabled()) {
				logger.debug("Available task handler: " + type + ", " + provider);
			}
			TaskHandlerNode.HANDLERS_CHANNEL.ret(stack, new TaskHandlerWrapper(provider,
					itype));
			if (includeAliases) {
				List<String> aliases = AbstractionProperties.getAliases(provider);
				for (String alias : aliases) {
					TaskHandlerNode.HANDLERS_CHANNEL.ret(stack, new TaskHandlerWrapper(alias,
							itype));
				}
			}
		}
	}
}
