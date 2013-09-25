/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.globus.cog.karajan.stack.VariableStack;
import org.griphyn.vdl.karajan.VDL2ExecutionContext;
import org.griphyn.vdl.karajan.monitor.SystemState;

public class ExecutionContextProcessor extends AbstractMessageProcessor {

	public Level getSupportedLevel() {
		return Level.INFO;
	}

	public Class<?> getSupportedSource() {
		return VDL2ExecutionContext.class;
	}

	public void processMessage(SystemState state, Object message, Object details) {
		if (message instanceof VariableStack) {
		    state.setStack((VariableStack) message);
		}
	}
}
