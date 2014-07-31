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
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import k.rt.Channel;
import k.rt.ChannelOperator;
import k.rt.MemoryChannel;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class Unique extends InternalFunction {
	
	private ChannelRef<Object> c_vargs;
    private ChannelRef<Object> cr_vargs;
    private List<Object> initial;

    @Override
    protected Signature getSignature() {
        return new Signature(params("..."), returns(channel("...", DYNAMIC)));
    }
    
    private static class Op extends ChannelOperator<Object, Object> {
    	private Set<Object> set;
    	private Channel<Object> dest;
    	        
        public Op(List<Object> values, Channel<Object> dest) {
            super(null);
            this.dest = dest;
            if (values != null) {
            	addAll(values);
            }
            set = new HashSet<Object>();
        }
        
        @Override
        protected Object update(Object crt, Object value) {
            // this should be synchronized by now
			if (!set.contains(value)) {
				set.add(value);
				dest.add(value);
			}
			return null;
        }

        @Override
        public String toString() {
            return "uniqueop";
        }
    }

    @Override
    protected void initializeArgs(Stack stack) {
        c_vargs.set(stack, new Op(initial, cr_vargs.get(stack)));
    }
    
    @Override
    protected ChannelRef<?> makeStaticChannel(int index, List<Object> values) {
        uniqueStatic(values);
        return new ChannelRef.Static<Object>(new MemoryChannel<Object>(values));
    }
    
    private void uniqueStatic(List<Object> values) {
        initial = new ArrayList<Object>();
        Set<Object> s = new HashSet<Object>();
        for (Object o : values) {
        	if (!s.contains(o)) {
        		s.add(o);
        		initial.add(o);
        	}
        }
    }

    @Override
    protected ChannelRef<?> makeMixedChannel(int index, List<Object> staticValues) {
        uniqueStatic(staticValues);
        return new ChannelRef.Dynamic<Object>("...", index);
    }

    @Override
    protected ChannelRef<Object> makeDynamicChannel(String name, int index) {
        return new ChannelRef.Dynamic<Object>("...", index);
    }

    @Override
    protected void runBody(LWThread thr) {
    }
}