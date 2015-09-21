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
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.griphyn.vdl.karajan.Command;
import org.griphyn.vdl.karajan.lib.cache.CacheMapAdapter;
import org.griphyn.vdl.mapping.DSHandle;

public class JobConstraints extends CacheFunction {
    private ArgRef<Command[]> commands;
    private ArgRef<Collection<DSHandle>> stagein;
    private ChannelRef<Object> cr_vargs;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("commands", optional("stagein", null)), returns(channel("...", 1)));
    }
	
	private static final String[] STRING_ARRAY = new String[0];

	@Override
    public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		Command[] commands = this.commands.getValue(stack);
		String[] filenames = null;
		Collection<DSHandle> stageins = this.stagein.getValue(stack);
		SwiftTaskConstraints tc = new SwiftTaskConstraints(commands);
		if (stageins != null) {
		    tc.setStageins(stageins);
		    tc.setFilecache(new CacheMapAdapter(getCache(stack)));
		}
		cr_vargs.append(stack, tc);
	}
	
	private static final List<String> NAMES1 = Arrays.asList("commands");
	private static final List<String> NAMES2 = Arrays.asList("commands", "stageins", "filecache");
	
	private static class SwiftTaskConstraints implements TaskConstraints {
	    
	    private final Command[] commands;
	    private Collection<DSHandle> stageins;
	    private CacheMapAdapter filecache;

	    public SwiftTaskConstraints(Command[] commands) {
	        this.commands = commands;
        }

        public Collection<DSHandle> getStageins() {
            return stageins;
        }

        public void setStageins(Collection<DSHandle> stageins) {
            this.stageins = stageins;
        }

        public CacheMapAdapter getFilecache() {
            return filecache;
        }


        public void setFilecache(CacheMapAdapter filecache) {
            this.filecache = filecache;
        }

        @Override
        public Object getConstraint(String name) {
            if ("commands".equals(name)) {
                return commands;
            }
            else if ("stageins".equals(name)) {
                return stageins;
            }
            else if ("filecache".equals(name)) {
                return filecache;
            }
            else {
                return null;
            }
        }
        
        @Override
        public Collection<String> getConstraintNames() {
            if (stageins == null) {
                return NAMES1;
            }
            else {
                return NAMES2;
            }
        }
	}
}
