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

import java.util.Iterator;
import java.util.List;

import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Future;
import k.rt.FutureListener;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.ThreadSet;
import k.thr.ThrottledThreadSet;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.UParallelFor;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.util.SwiftConfig;

public class ThrottledParallelFor extends UParallelFor {
	public static final Logger logger = Logger
			.getLogger(ThrottledParallelFor.class);
	
	public static final int DEFAULT_MAX_THREADS = 1024;

	private ArgRef<Boolean> selfClose;
	private ArgRef<String> refs;
	private ArgRef<String> _kvar;
	private ArgRef<String> _vvar;
	private ArgRef<Integer> _traceline;
	
	private boolean sc;
	    
    @Override
    protected Signature getSignature() {
        return new Signature(
            params(
                identifier("name"), "in", 
                optional("selfClose", Boolean.FALSE), optional("refs", null),
                optional("_kvar", null), optional("_vvar", null), optional("_traceline", null),
                block("body")
            )
        );
    }

	private int maxThreadCount = -1;
	private Tracer forTracer, iterationTracer;
    private List<StaticRefCount> srefs;

    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        srefs = StaticRefCount.build(scope, this.refs.getValue());
        if (_traceline.getValue() != null) {
            setLine(_traceline.getValue());
        }
        forTracer = Tracer.getTracer(this, "FOREACH");
        iterationTracer = Tracer.getTracer(this, "ITERATION");
        sc = selfClose.getValue();
        return super.compileBody(w, argScope, scope);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void runBody(final LWThread thr) {        
        int i = thr.checkSliceAndPopState();
        Iterator<Object> it = (Iterator<Object>) thr.popState();
        TPFThreadSet ts = (TPFThreadSet) thr.popState();
        int fc = thr.popIntState();
        List<RefCount> drefs = (List<RefCount>) thr.popState();
        Stack stack = thr.getStack();
        try {
            switch(i) {
                case 0:
                    it = in.getValue(stack).iterator();
                    if (sc) {
                        ts = new TPFSCThreadSet(it, getMaxThreads());
                    }
                    else {
                        ts = new TPFThreadSet(it, getMaxThreads());
                    }
                    
                    drefs = RefCount.build(stack, srefs);
                    ts.lock();
                    fc = stack.frameCount() + 1;
                    
                    if (forTracer.isEnabled()) {
                        forTracer.trace(thr);
                    }
                    
                    i++;
                case 1:
                    final ThreadSet tsf = ts;
                    
                    ts.checkFailed();
                    
                    startBulk(thr, ts, fc, drefs);
                    startRest(thr, ts, fc, drefs);
                    
                    ts.unlock();
                    RefCount.decRefs(drefs);
                    i++;
                case 2:
                    ts.waitFor();
            }
        }
        catch (Yield y) {
            y.getState().push(drefs);
            y.getState().push(fc);
            y.getState().push(ts);
            y.getState().push(it);
            y.getState().push(i);
            throw y;
        }
    }

	private boolean startBulk(LWThread thr, TPFThreadSet ts, int fcf, List<RefCount> refs) {
	    int available = ts.freeSlots();
	    int j = 0;
	    Stack stack = thr.getStack();
	    for (; j < available && ts.hasNext(); j++) {
	        if (startOne(thr, ts, ts.next(), fcf, refs)) {
                // aborted
                return true;
            }
        }
        return false;
    }
	
	private boolean startRest(LWThread thr, TPFThreadSet ts, int fcf, List<RefCount> refs) {
        Stack stack = thr.getStack();
        while (ts.hasNext()) {
            ts.waitForSlot();
            if (startOne(thr, ts, ts.next(), fcf, refs)) {
                return true;
            }
        }
        return false;
    }

    private boolean iteratorHasValues(Iterator<Object> it) {
        try {
            it.hasNext();
            return true;
        }
        catch (ConditionalYield y) {
            return false;
        }
    }

    private boolean startOne(final LWThread thr, final ThreadSet ts, final Object value, final int fcf, List<RefCount> refs) {
        RefCount.incRefs(refs);
        LWThread ct = thr.fork(new KRunnable() {
            @Override
            public void run(LWThread thr2) {
                int i = thr2.checkSliceAndPopState();
                try {
                    switch (i) {
                        case 0:
                            if (iterationTracer.isEnabled()) {
                                iterationTracer.trace(thr2, unwrap(value));
                            }

                            if (CompilerSettings.PERFORMANCE_COUNTERS) {
                                startCount++;
                            }
                            i++;
                        case 1:
                            body.run(thr2);
                            ts.threadDone(thr2, null);
                    }
                }
                catch (ExecutionException e) {
                    thr2.getStack().dropToFrame(fcf);
                    ts.threadDone(thr2, e);
                    ts.abortAll();
                    thr.awake();
                }
                catch (Exception e) {
                    thr2.getStack().dropToFrame(fcf);
                    ts.threadDone(thr2, new ExecutionException(ThrottledParallelFor.this, e));
                    ts.abortAll();
                    thr.awake();
                }
                catch (Yield y) {
                    y.getState().push(i);
                    throw y;
                }
            }
        });
        if(ts.add(ct)) {
            return true;
        }
        
        Stack cs = ct.getStack();
        cs.enter(this, frameSize);
        this.var.setValue(cs, value);
        ct.start();
        return false;
    }

	
	private int getMaxThreads() {
	    if (maxThreadCount < 0) {
            maxThreadCount = SwiftConfig.getDefault().getForeachMaxThreads();
        }
	    return maxThreadCount;
	}
	
	protected Object unwrap(Object value) {
        if (value instanceof List) {
            List<?> p = (List<?>) value;
            if (_kvar.getValue() != null) {
                return _kvar.getValue() + "=" + p.get(0) + ", " + _vvar.getValue() + "=" + Tracer.unwrapHandle(p.get(1));
            }
            else {
                return _vvar.getValue() + "=" + Tracer.unwrapHandle(p.get(1));
            }
        }
        else {
            return "!";
        }
    }


	@Override
    public String getTextualName() {
        return "foreach";
    }
	
	private static class TPFThreadSet extends ThrottledThreadSet {
	    protected final Iterator<Object> it;
	    
	    public TPFThreadSet(Iterator<Object> it, int maxThreads) {
	        super(maxThreads);
	        this.it = it;
	    }

        public boolean hasNext() {
            return it.hasNext();
        }
        
        public Object next() {
            return it.next();
        }
	}
	
	private static class TPFSCThreadSet extends TPFThreadSet implements FutureListener {
	    private Helper helper;
	    private boolean closed;
	    
	    public TPFSCThreadSet(Iterator<Object> it, int maxThreads) {
	        super(it, maxThreads);
	        helper = new Helper();
	    }
	    
	    public synchronized boolean hasNext() {
	        if (closed) {
	            return false;
	        }
	        try {
	            return it.hasNext();
	        }
	        catch (ConditionalYield y) {
	            helper.resetItUpdated();
	            y.getFuture().addListener(this, y);
	            throw new ConditionalYield(helper);
	        }
	    }

        @Override
        public synchronized void threadDone(LWThread thr, ExecutionException e) {
            super.threadDone(thr, e);
            if (getRunning() == 1 && !iteratorHasValues()) {
                closed = true;
                helper.awake();
            }
        }

        private boolean iteratorHasValues() {
            try {
                it.hasNext();
                return true;
            }
            catch (Yield y) {
                return false;
            }
        }

        @Override
        public void futureUpdated(Future fv) {
            helper.awake();
        }
	}
	
	private static class Helper implements Future {
	    private FutureListener l;
	    private boolean itUpdated;

        @Override
        public synchronized void addListener(FutureListener l, ConditionalYield y) {
            if (itUpdated) {
                l.futureUpdated(this);
            }
            else {
                this.l = l;
            }
        }

        public synchronized void resetItUpdated() {
            this.itUpdated = false;
        }

        public synchronized void awake() {
            if (l == null) {
                this.itUpdated = true;
            }
            else {
                FutureListener l = this.l;
                this.l = null;
                l.futureUpdated(this);
            }
        }
	}
}
