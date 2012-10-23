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
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;

public class Log extends AbstractSequentialWithArguments {
	public static final Arg LEVEL = new Arg.Positional("level");
	public static final Arg MESSAGE = new Arg.Optional("message", null);

	static {
		setArguments(Log.class, new Arg[] { LEVEL, MESSAGE, Arg.VARGS });
	}

	public static final Logger logger = Logger.getLogger("swift");
	private static final Map<String, Level> priorities = new HashMap<String, Level>();

	static {
		priorities.put("debug", Level.DEBUG);
		priorities.put("info", Level.INFO);
		priorities.put("warn", Level.WARN);
		priorities.put("error", Level.ERROR);
		priorities.put("fatal", Level.FATAL);
	}

	public static Level getLevel(String lvl) {
		return priorities.get(lvl);
	}
	
	protected void post(VariableStack stack) throws ExecutionException {
		Level lvl = getLevel((String) LEVEL.getValue(stack));
		if (logger.isEnabledFor(lvl)) {
		    Object smsg = MESSAGE.getValue(stack);
		    if (smsg != null) {
		        logger.log(lvl, smsg);
		    }
		    else {
		        Object[] msg = Arg.VARGS.asArray(stack);
		        StringBuilder sb = new StringBuilder();
		        for (int i = 0; i < msg.length; i++) {
		            sb.append(TypeUtil.toString(msg[i]));
		        }
		        logger.log(lvl, sb.toString());
		    }
		}
		super.post(stack);
	}
}
