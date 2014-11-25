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
package org.griphyn.vdl.karajan.monitor.processors.swift;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.processors.AbstractMessageProcessor;

public class CompletionProcessor extends AbstractMessageProcessor {
    
	public Level getSupportedLevel() {
		return Level.INFO;
	}

	public Class<?> getSupportedSource() {
		return Loader.class;
	}

	public void processMessage(SystemState state, Object message, Object details) {
		String msg = String.valueOf(message);
		if (msg.startsWith("Swift finished")) {
		    state.setRunFinished(true);
		}
	}	
}
