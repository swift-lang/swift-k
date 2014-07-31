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


package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.PathElementComparator;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;

public class ExpandArguments extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(ExpandArguments.class);
	
	private ChannelRef<Object> c_vargs;

	@Override
    protected Signature getSignature() {
        return new Signature(params("..."));
    }

	public Object function(Stack stack) {
	    Channel<Object> items = c_vargs.get(stack);
	    // prefer lower memory usage over speed here
	    // although the penalty is only there if there are arrays in the arguments
	    if (hasArrays(items)) {
	        return expandArrays(items);
	    }
	    else {
	        return items.getAll();
	    }
	}

	private Object expandArrays(Channel<Object> items) {
        ArrayList<DSHandle> l = new ArrayList<DSHandle>();
        for (Object item : items) {
            AbstractDataNode h = (AbstractDataNode) item;
            h.waitFor(this);
            if (item instanceof ArrayHandle) {
                ArrayHandle array = (ArrayHandle) item;
                Map<Comparable<?>, DSHandle> m = array.getArrayValue();
                SortedMap<Comparable<?>, DSHandle> sorted = new TreeMap<Comparable<?>, DSHandle>(new PathElementComparator());
                sorted.putAll(m);
                l.addAll(m.values());
            } 
            else {
                l.add(h);
            }
            // TODO this does not correctly handle structs or
            // externals - at the moment, probably neither of
            // those should be usable as a string. It also
            // does not handle nested arrays. However, none of
            // those should get here in normal operation due
            // to static type-checking
        }
        return l;
    }

    private boolean hasArrays(Channel<Object> items) {
	    boolean arraySeen = false;
        for (Object item : items) {
            if(!(item instanceof DSHandle)) {
                throw new ExecutionException(this, "Cannot handle argument implemented by " + item.getClass());
            }

            if (item instanceof ArrayHandle) {
                arraySeen = true;
            } 
        }
        return arraySeen;
    }
}

