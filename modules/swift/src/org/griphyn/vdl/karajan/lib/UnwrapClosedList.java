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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class UnwrapClosedList extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(UnwrapClosedList.class);
	
	private ArgRef<List<AbstractDataNode>> list;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("list"));
    }

	@Override
	public Object function(Stack stack) {
        List<AbstractDataNode> l = this.list.getValue(stack);
		
		List<Object> r = new ArrayList<Object>(l.size());
		
		for (AbstractDataNode h : l) {
		    if (h.getType().isArray()) {
		        h.waitFor(this);
		        Map<?, DSHandle> m = h.getArrayValue();
		        for (DSHandle h2 : m.values()) {
		            if (h2.getType().isPrimitive()) {
		                ((AbstractDataNode) h2).waitFor(this);
		                r.add(h2.getValue());
		            }
		            else {
		                throw new ExecutionException(this, "Cannot pass an array of non-primitives as an application parameter");
		            }
		        }
		    }
		    else if (h.getType().isPrimitive()) {
		        // stagein only waits for the root of the parameter
		        // and the fringes as returned by getFringePaths(), but
		        // the latter only returns mapped types
		        h.waitFor(this);
		        r.add(h.getValue());
		    }
		    else {
		        throw new ExecutionException(this, "Cannot pass a structure as an application parameter");
		    }
		}
		
		return r;
	}
}
