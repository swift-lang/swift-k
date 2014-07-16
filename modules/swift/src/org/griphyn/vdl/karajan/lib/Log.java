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

import k.rt.Channel;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class Log extends InternalFunction {
	private ArgRef<String> level;
	private ChannelRef<Object> c_vargs;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("level", "..."));
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
    
    
    
    @Override
    public Node compile(WrapperNode w, Scope scope) throws CompilationException {
        Node n = super.compile(w, scope);
        String sLvl = this.level.getValue();
        if (sLvl != null) {
            // don't compile this if it won't produce output
            if (!logger.isEnabledFor(getLevel(sLvl))) {
                return null;
            }
        }
        return n;
    }


    protected void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		Level lvl = getLevel(this.level.getValue(stack));
		if (logger.isEnabledFor(lvl)) {
		    Channel<Object> l = c_vargs.get(stack);
		    StringBuilder sb = new StringBuilder();
		    for (Object o : l) {
		        sb.append(o);
		    }
		    logger.log(lvl, sb.toString());
		}
	}
}
