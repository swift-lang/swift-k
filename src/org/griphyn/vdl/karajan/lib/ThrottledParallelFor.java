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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureIteratorIncomplete;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.ListenerStackPair;
import org.globus.cog.karajan.workflow.nodes.AbstractParallelIterator;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.util.VDL2Config;

public class ThrottledParallelFor extends AbstractParallelIterator {
	public static final Logger logger = Logger.getLogger(ThrottledParallelFor.class);
	
	public static final int DEFAULT_MAX_THREADS = 10000000;

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_IN = new Arg.Positional("in");
	public static final Arg O_SELFCLOSE = new Arg.Optional("selfclose", Boolean.FALSE);
	public static final Arg O_REFS = new Arg.Optional("refs", null);

	static {
		setArguments(ThrottledParallelFor.class, new Arg[] { A_NAME, A_IN, O_SELFCLOSE, O_REFS });
	}

	public static final String THREAD_COUNT = "#threadcount";

	private int maxThreadCount = -1;
	private Tracer forTracer, iterationTracer;
	private String kvar, vvar;
	private List<StaticRefCount> srefs;
	
	@Override
    protected void initializeStatic() {
        super.initializeStatic();
        forTracer = Tracer.getTracer(this, "FOREACH");
        iterationTracer = Tracer.getTracer(this, "ITERATION");
        kvar = (String) getProperty("_kvar");
        vvar = (String) getProperty("_vvar");
        srefs = StaticRefCount.build((String) O_REFS.getStatic(this));
    }

    protected void partialArgumentsEvaluated(VariableStack stack)
            throws ExecutionException {
        if (forTracer.isEnabled()) {
            forTracer.trace(ThreadingContext.get(stack).toString());
        }
        super.partialArgumentsEvaluated(stack);
    }

    public void iterate(VariableStack stack, Identifier var, KarajanIterator i)
			throws ExecutionException {
		if (elementCount() > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("iterateParallel: " + stack.parentFrame());
			}
			stack.setVar(VAR, var);
			setChildFailed(stack, false);
			stack.setCaller(this);
			initThreadCount(stack, TypeUtil.toBoolean(O_SELFCLOSE.getStatic(this)), i);
			citerate(stack, var, i);
		}
		else {
			complete(stack);
		}
	}

	protected void citerate(VariableStack stack, Identifier var,
			KarajanIterator i) throws ExecutionException {
		ThreadCount tc = getThreadCount(stack);
		
		// we can bulk operations at the start to avoid contention
		// on the counter since at least as many
		// threads as reported by available() are available
		int available = tc.available();
		try {
		    int j = 0;
		    synchronized(tc) {
    		    try {
        		    for (; j < available && i.hasNext(); j++) {
        		        startIteration(tc, var, i.current(), i.next(), stack);
        		    }
    		    }
    		    finally {
    		        tc.add(j);
    		    }
		    }
			while (i.hasNext()) {
			    startIteration(tc, var, i.current(), tc.tryIncrement(), stack);
			}
			
			if (!tc.selfClose) {
			    decRefs(tc.rc);
			}
			
			int left;
			synchronized(tc) {
			    // can only have closed and running = 0 in one of the two critical sections
			    // related to tc. One (CS1) is this and the other one (CS2) is:
			    // closed = tc.isClosed();
			    // running = tc.decrement();
			    // 
			    // If on the last iteration CS1 runs before CS2, then:
			    // CS1 - (closed = true, left > 0), CS2 - (closed = true, --running == 0)
			    // If CS2 runs before CS1:
			    // CS2 - (closed = false, --running == 0), CS1 - (closed = true, left == 0).
			    tc.close();
			    left = tc.current();
			}
			if (left == 0) {
			    // i.e., no iteration running
			    if (!tc.selfClose) {
			        // when selfClose is on, decRefs() and complete() are called
			        // as part of the self closing logic, so don't invoke them again
			        complete(stack);
			    }
			}
			else {
			    // complete() will be called when
			    // the last iteration completes (in iterationCompleted).
			}
		}
		catch (FutureIteratorIncomplete fii) {
			synchronized (stack.currentFrame()) {
                stack.setVar(ITERATOR, i);
            }
            fii.getFutureIterator().addModificationAction(this, stack);
		}
	}
	
	private void startIteration(ThreadCount tc, Identifier var, int id, Object value,
            VariableStack stack) throws ExecutionException {
	    incRefs(tc.rc);
        VariableStack copy = stack.copy();
        copy.enter();
        ThreadingContext ntc = ThreadingContext.get(copy).split(id);
        ThreadingContext.set(copy, ntc);
        setIndex(copy, 2);
        if (iterationTracer.isEnabled()) {
            iterationTracer.trace(ntc.toString(), unwrap(value));
        }
        copy.setVar(var.getName(), value);
        startElement(1, copy);
    }

    private void decRefs(List<RefCount> rcs) throws ExecutionException {
	    if (rcs != null) {
	        for (RefCount rc : rcs) {
	            rc.var.updateWriteRefCount(-rc.count);
	        }
	    }
	}
	
	private void incRefs(List<RefCount> rcs) throws ExecutionException {
        if (rcs != null) {
            for (RefCount rc : rcs) {
                rc.var.updateWriteRefCount(rc.count);
            }
        }
    }

    private Object unwrap(Object value) {
        if (value instanceof Pair) {
            Pair p = (Pair) value;
            if (kvar != null) {
                return kvar + "=" + p.get(0) + ", " + vvar + "=" + Tracer.unwrapHandle(p.get(1));
            }
            else {
                return vvar + "=" + Tracer.unwrapHandle(p.get(1));
            }
        }
        else {
            return "!";
        }
    }
	
    @Override
    public void completed(VariableStack stack) throws ExecutionException {
        int index = preIncIndex(stack) - 1;
        if (index == 1) {
            // iterator
            stack.currentFrame().deleteVar(QUOTED);
            processArguments(stack);
            try {
                partialArgumentsEvaluated(stack);
            }
            catch (FutureFault e) {
                e.getFuture().addModificationAction(new PartialResume(), stack);
            }
        }
        else if (index == elementCount()) {
            iterationCompleted(stack);
        }
        else {
            startElement(index, stack);
        }
    }

    public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
        if (!testAndSetChildFailed(stack)) {
            if (stack.parentFrame().isDefined(VAR)) {
                stack.leave();
            }
            failImmediately(stack, e);
        }
    }

	protected void iterationCompleted(VariableStack stack)
			throws ExecutionException {
		stack.leave();
		ThreadCount tc = getThreadCount(stack);
		int running;
		boolean closed;
		boolean iteratorHasValues;
		synchronized(tc) {
		    closed = tc.isClosed();
		    running = tc.decrement();
		    iteratorHasValues = tc.iteratorHasValues();
		}
		boolean done = false;
		if (running == 0) {
		    if (closed) {
		        // write refs were decremented when the last
		        // iteration was started
		        complete(stack);
		    }
		    else if (tc.selfClose && !iteratorHasValues) {
		        decRefs(tc.rc);
		        complete(stack);
		    }
		}
	}

	private void initThreadCount(VariableStack stack, boolean selfClose, KarajanIterator i) throws VariableNotFoundException {
		if (maxThreadCount < 0) {
			try {
				maxThreadCount = TypeUtil.toInt(VDL2Config.getConfig()
						.getProperty("foreach.max.threads", String.valueOf(DEFAULT_MAX_THREADS)));
			}
			catch (IOException e) {
				maxThreadCount = DEFAULT_MAX_THREADS;
			}
		}
		stack.setVar(THREAD_COUNT, new ThreadCount(maxThreadCount, selfClose, i, RefCount.build(srefs, stack)));
	}

	private ThreadCount getThreadCount(VariableStack stack)
			throws VariableNotFoundException {
		return (ThreadCount) stack.getVar(THREAD_COUNT);
	}
	
	@Override
    public String getTextualName() {
        return "foreach";
    }

    private static class ThreadCount implements FutureIterator {
		public boolean selfClose;
        private int maxThreadCount;
		private int crt;
		private boolean closed;
		private List<ListenerStackPair> listeners;
		private KarajanIterator i;
		private final List<RefCount> rc;

		public ThreadCount(int maxThreadCount, boolean selfClose, KarajanIterator i, List<RefCount> rc) {
			this.maxThreadCount = maxThreadCount;
			this.i = i;
			crt = 0;
			this.selfClose = selfClose;
			this.rc = rc;
		}
		
		public boolean raiseWaiting() {
            return false;
        }

        public boolean iteratorHasValues() {
            try {
                return i.hasNext();
            }
            catch (FutureFault e) {
                return false;
            }
        }
        
        public int available() {
            return maxThreadCount - crt;
        }
        
        public void add(int count) {
            crt += count;
        }

		public synchronized Object tryIncrement() {
		    // there is no way that both crt == 0 and i has no values outside this critical section
			if (crt < maxThreadCount) {
				Object o = i.next();
				crt++;
				return o;
			}
			else {
				throw new FutureIteratorIncomplete(this, this);
			}
		}

		public synchronized int decrement() {
			crt--;
			notifyListeners();
			return crt;
		}

		private void notifyListeners() {
			if (listeners != null) {
				Iterator<ListenerStackPair> i = listeners.iterator();
				listeners = null;
				while (i.hasNext()) {
					ListenerStackPair etp = i.next();
					i.remove();
					etp.listener.futureModified(this, etp.stack);
				}
			}
		}

		public boolean hasAvailable() {
			return false;
		}

		public int count() {
			return 0;
		}

		public synchronized int current() {
			return crt;
		}

		public Object peek() {
			return null;
		}

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			return null;
		}

		public void remove() {
		}

		public synchronized void addModificationAction(FutureListener target,
				VariableStack stack) {
			if (listeners == null) {
				listeners = new ArrayList<ListenerStackPair>();
			}
			listeners.add(new ListenerStackPair(target, stack));
			if (crt < maxThreadCount) {
				notifyListeners();
			}
		}

		public synchronized void close() {
		    this.closed = true;
		}

		public void fail(FutureEvaluationException e) {
		}

		public Object getValue() {
			return null;
		}

		public synchronized boolean isClosed() {
		    return closed;
		}
	}
}
