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
import org.griphyn.vdl.karajan.monitor.processors.ParsingException;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class ConfigurationProcessor extends AbstractMessageProcessor {
    
	public Level getSupportedLevel() {
		return Level.DEBUG;
	}

	public Class<?> getSupportedSource() {
		return Loader.class;
	}

	public void processMessage(SystemState state, Object message, Object details) {
		String msg = String.valueOf(message);
		SimpleParser p = new SimpleParser(msg);
		try {
    		if (p.matchAndSkip("SWIFT_CONFIGURATION")) {
    		    p.skipTo(": {");
    		    p.beginToken();
    		    p.markTo("}");
    		    String options = p.getToken();
    		    String[] els = options.split(",\\s*");
    		    for (String el : els) {
    		        String[] kv = el.split("=", 2);
    		        if (kv[0].equals("execution.retries")) {
    		            state.setRetries(Integer.parseInt(kv[1]));
    		        }
    		        else if (kv[0].equals("replication.enabled")) {
    		            state.setReplicationEnabled(Boolean.parseBoolean(kv[1]));
    		        }
    		    }
    		}
		}
		catch (ParsingException e) {
		    e.printStackTrace();
		}
	}	
}
