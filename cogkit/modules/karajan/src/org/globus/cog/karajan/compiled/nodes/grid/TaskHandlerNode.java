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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.util.TaskHandlerWrapper;

public class TaskHandlerNode extends AbstractFunction {

	private static final Map<String, String> ptypes;
	private static final Map<String, Integer> wtypes;

	static {
		ptypes = new HashMap<String, String>();
		ptypes.put("execution", AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER);
		ptypes.put("job-submission", AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER);
		ptypes.put("file-transfer", AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER);
		ptypes.put("file", AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER);
		ptypes.put("file-operation", AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER);

		wtypes = new HashMap<String, Integer>();
		wtypes.put(AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER, TaskHandler.EXECUTION);
		wtypes.put(AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER, TaskHandler.FILE_TRANSFER);
		wtypes.put(AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER, TaskHandler.FILE_OPERATION);
	}

	public static String karajanToAbstractionType(String type) {
		String atype = ptypes.get(type);
		if (atype == null) {
			throw new ExecutionException("Unsupported type: " + type
					+ ". Supported types are: \"execution\", \"file\", and \"file-transfer\"");
		}
		else {
			return atype;
		}
	}

	public static int abstractionToHandlerType(String type) {
		try {
			return wtypes.get(type);
		}
		catch (NullPointerException e) {
			throw new ExecutionException("Invalid abstraction handler type: " + type);
		}
	}
		
	public static int karajanToHandlerType(String type) {
		return abstractionToHandlerType(karajanToAbstractionType(type));
	}
	
	private ArgRef<String> type;
	private ArgRef<String> provider;
	private ChannelRef<Object> cr_handlers;

	@Override
	protected Signature getSignature() {
		return new Signature(params("type", "provider"), returns(channel("handlers")));
	}

	public Object function(Stack stack) throws ExecutionException {
		TaskHandlerWrapper th = new TaskHandlerWrapper();
		String type = this.type.getValue(stack).toLowerCase();
		String provider = this.provider.getValue(stack);
		
		th.setType(karajanToHandlerType(type));
		th.setProvider(provider);

		cr_handlers.append(stack, th);
		return null;
	}
}