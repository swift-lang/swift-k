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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;
import org.globus.cog.karajan.workflow.nodes.AbstractUParallelIterator;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.FlowNode.FNTP;

public class SelfCloseParallelFor extends AbstractUParallelIterator {
	public static final Logger logger = Logger
			.getLogger(SelfCloseParallelFor.class);
	
	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_IN = new Arg.Positional("in");
	public static final Arg A_SELF_CLOSE = new Arg.Optional("selfclose", Boolean.FALSE);

	static {
		setArguments(SelfCloseParallelFor.class, new Arg[] { A_NAME, A_IN, A_SELF_CLOSE });
	}

    protected void partialArgumentsEvaluated(VariableStack stack)
            throws ExecutionException {
        stack.setVar("selfclose", A_SELF_CLOSE.getValue(stack));
        super.partialArgumentsEvaluated(stack);
    }
    
    protected void citerate(VariableStack stack, Identifier var, KarajanIterator i)
            throws ExecutionException {
        try {
            synchronized(stack.currentFrame()) {
                while (i.hasNext()) {
                    Object value = i.next();
                    VariableStack copy = stack.copy();
                    copy.enter();
                    ThreadingContext.set(copy, ThreadingContext.get(copy).split(i.current()));
                    setIndex(copy, getArgCount());
                    setArgsDone(copy);
                    copy.setVar(var.getName(), value);
                    int r = preIncRunning(stack);
                    startElement(getArgCount(), copy);
                }
            }
            if (FlowNode.debug) {
                threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
            }
            // Now make sure all iterations have not completed
            int left = preDecRunning(stack);
            if (left == 0) {
                complete(stack);
            }
        }
        catch (FutureIteratorIncomplete fii) {
            synchronized (stack.currentFrame()) {
                // if this is defined, then resume from iterate
                stack.setVar(ITERATOR, i);
            }
            fii.getFutureIterator().addModificationAction(this, stack);
        }
    }
    
    protected void iterationCompleted(VariableStack stack) throws ExecutionException {
        stack.leave();
        int running;
        synchronized(stack.currentFrame()) {
            running = preDecRunning(stack);
            if (running == 1) {
                KarajanIterator iter = (KarajanIterator) stack.currentFrame().getVar(ITERATOR);
                if (stack.currentFrame().getVar("selfclose").equals(Boolean.TRUE)) {
                    try {
                        iter.hasNext();
                    }
                    catch (FutureFault f) {
                        running = 0;
                    }
                }
            }
        }
        if (running == 0) {
            complete(stack);
        }
    }
    		
	@Override
    public String getTextualName() {
        return "foreach";
	}
}
