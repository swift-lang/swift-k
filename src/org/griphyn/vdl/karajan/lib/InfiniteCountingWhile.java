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

import java.util.Collections;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.globus.cog.karajan.workflow.nodes.While;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

public class InfiniteCountingWhile extends Sequential {
    
    public static final String COUNTER_NAME = "$";
    public static final Arg.Positional VAR = new Arg.Positional("var");

	public InfiniteCountingWhile() {
		setOptimize(false);
	}

	public void pre(VariableStack stack) throws ExecutionException {
		ThreadingContext tc = (ThreadingContext)stack.getVar("#thread");
		stack.setVar("#iteratethread", tc);
		stack.setVar("#thread", tc.split(0));
		stack.setVar(COUNTER_NAME, Collections.singletonList(0));
		stack.setVar((String) VAR.getStatic(this), new RootDataNode(Types.INT, 0));
		super.pre(stack);
	}

	protected void startNext(VariableStack stack) throws ExecutionException {
		if (stack.isDefined("#abort")) {
			abort(stack);
			return;
		}
		int index = getIndex(stack);
		if (elementCount() == 0) {
			post(stack);
			return;
		}
		FlowElement fn = null;
		
		if (index == elementCount() - 1) {
		    // the condition is always compiled as the last thing in the loop
		    // but the increment needs to happen before the condition is
		    // evaluated
		    @SuppressWarnings("unchecked")
		    List<Integer> c = (List<Integer>) stack.getVar(COUNTER_NAME);
            int i = c.get(0).intValue();
            i++;
            ThreadingContext tc = (ThreadingContext)stack.getVar("#iteratethread");
            stack.setVar("#thread", tc.split(i));
            stack.setVar(COUNTER_NAME, Collections.singletonList(i));
            stack.setVar((String) VAR.getStatic(this), new RootDataNode(Types.INT, i));
		}
		if (index >= elementCount()) {
			// starting new iteration
			setIndex(stack, 1);
			fn = getElement(0);
		}
		else {
			fn = getElement(index++);
			setIndex(stack, index);
		}
		startElement(fn, stack);
	}
	
    public void failed(VariableStack stack, ExecutionException e)
            throws ExecutionException {
        if (e instanceof While.Break) {
        	complete(stack);
        	return;
        }
        if (e instanceof While.Continue) {
        	setIndex(e.getStack(), 0);
            startNext(e.getStack());
            return;
        }
        super.failed(stack, e);
    }

    @Override
    public String getTextualName() {
        return "iterate";
    }
    
    
}
