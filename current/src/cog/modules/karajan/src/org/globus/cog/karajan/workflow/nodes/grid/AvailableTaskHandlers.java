//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2007
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.Iterator;
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

	static {
		setArguments(AvailableTaskHandlers.class, new Arg[] { A_TYPE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		List providers;
		if (A_TYPE.isPresent(stack)) {
			getHandlers(stack, TypeUtil.toString(A_TYPE.getValue(stack)));
		}
		else {
			getHandlers(stack, "execution");
			getHandlers(stack, "file");
			getHandlers(stack, "file-transfer");
		}
		return null;
	}

	protected void getHandlers(VariableStack stack, String type) throws ExecutionException {
		String atype = TaskHandlerNode.karajanToAbstractionType(type);
		int itype = TaskHandlerNode.abstractionToHandlerType(atype);

		List providers = AbstractionProperties.getProviders(atype);
		Iterator i = providers.iterator();
		while (i.hasNext()) {
			String provider = (String) i.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Available task handler: " + type + ", " + provider);
			}
			TaskHandlerNode.HANDLERS_CHANNEL.ret(stack, new TaskHandlerWrapper(provider,
					itype));
		}
	}
}
