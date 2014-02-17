//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2007
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.util.TaskHandlerWrapper;

public class AvailableTaskHandlers extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(AvailableTaskHandlers.class);
	
	private ArgRef<String> type;
	private ArgRef<Boolean> includeAliases;
	private ChannelRef<TaskHandlerWrapper> cr_handlers;

	@Override
	protected Signature getSignature() {
		return new Signature(params(optional("type", null), optional("includeAliases", Boolean.FALSE)), returns(channel("handlers", DYNAMIC)));
	}

	@Override
	public Object function(Stack stack) {
		boolean aliases = includeAliases.getValue(stack);
		String type = this.type.getValue(stack);
		if (type != null) {
			getHandlers(stack, type, aliases);
		}
		else {
			getHandlers(stack, "execution", aliases);
			getHandlers(stack, "file", aliases);
			getHandlers(stack, "file-transfer", aliases);
		}
		return null;
	}

	protected void getHandlers(Stack stack, String type, boolean includeAliases) throws ExecutionException {
		String atype = TaskHandlerNode.karajanToAbstractionType(type);
		int itype = TaskHandlerNode.abstractionToHandlerType(atype);

		List<String> providers = AbstractionProperties.getProviders(atype);
		for (String provider : providers) {
			if (logger.isDebugEnabled()) {
				logger.debug("Available task handler: " + type + ", " + provider);
			}
			cr_handlers.append(stack, new TaskHandlerWrapper(provider, itype));
			if (includeAliases) {
				List<String> aliases = AbstractionProperties.getAliases(provider);
				for (String alias : aliases) {
					cr_handlers.append(stack, new TaskHandlerWrapper(alias, itype));
				}
			}
		}
	}
}
