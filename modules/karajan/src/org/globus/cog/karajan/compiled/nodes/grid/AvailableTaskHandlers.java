/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
