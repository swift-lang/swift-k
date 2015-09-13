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
import java.util.LinkedList;
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
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.PartialCloseable;
import org.griphyn.vdl.mapping.nodes.ReadRefWrapper;
import org.griphyn.vdl.mapping.nodes.RootClosedPrimitiveDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.util.SwiftConfig;

public class ThrottledParallelFor extends InternalFunction {
	public static final Logger logger = Logger
			.getLogger(ThrottledParallelFor.class);
	
	public static final int DEFAULT_MAX_THREADS = 1024;

	private ArgRef<Iterable<List<?>>> in;
	private VarRef<DSHandle> kvarRef;
	private VarRef<DSHandle> vvarRef;
	private ArgRef<Boolean> selfClose;
	private ArgRef<String> wrefs, rrefs;
	private ArgRef<String> _kvar;
	private ArgRef<String> _vvar;
	private ArgRef<Field> _kvarField;
	private Field indexVarField;
	protected Node body;
	
	private boolean sc;
	    
    @Override
    protected Signature getSignature() {
        return new Signature(
            params(
                "in", 
                optional("selfClose", Boolean.FALSE), optional("wrefs", null), optional("rrefs", null),
                optional("_kvar", null), optional("_kvarField", null), optional("_vvar", null),
                block("body")
            )
        );
    }

	private int maxThreadCount = -1;
	private Tracer forTracer, iterationTracer;
    private List<StaticRefCount<PartialCloseable>> swrefs;
    private List<StaticRefCount<ReadRefWrapper>> srrefs;
    private int frameSize;
    
    @Override
    protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
            Scope scope) throws CompilationException {
        DynamicScope ds = new DynamicScope(w, scope);
        ContainerScope cs = new ContainerScope(w, ds);
        Var vvar = cs.addVar(_vvar.getValue());
        vvarRef = cs.getVarRef(vvar);
        
        if (_kvar.getValue() != null) {
            Var kvar = cs.addVar(_kvar.getValue());
            kvarRef = cs.getVarRef(kvar);
            indexVarField = _kvarField.getValue();
        }
        
        super.compileBlocks(w, sig, blocks, cs);
        
        frameSize = cs.size();
        
        ds.close();
    }

    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        swrefs = StaticRefCount.build(scope, this.wrefs.getValue(), false);
        srrefs = StaticRefCount.build(scope, this.rrefs.getValue(), true);
        forTracer = Tracer.getTracer(this, "FOREACH");
        iterationTracer = Tracer.getTracer(this, "ITERATION");
        sc = selfClose.getValue();
        return super.compileBody(w, argScope, scope);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void runBody(final LWThread thr) {        
        int i = thr.checkSliceAndPopState(2);
        Iterator<List<?>> it = (Iterator<List<?>>) thr.popState();
        TPFThreadSet ts = (TPFThreadSet) thr.popState();
        int fc = thr.popIntState();
        List<RefCount<PartialCloseable>> dwrefs = (List<RefCount<PartialCloseable>>) thr.popState();
        List<RefCount<ReadRefWrapper>> drrefs = (List<RefCount<ReadRefWrapper>>) thr.popState();
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
                    
                    dwrefs = RefCount.build(stack, swrefs);
                    drrefs = RefCount.build(stack, srrefs);
                    ts.lock();
                    fc = stack.frameCount() + 1;
                    
                    if (forTracer.isEnabled()) {
                        forTracer.trace(thr);
                    }
                    
                    i++;
                case 1:
                    final ThreadSet tsf = ts;
                    
                    ts.checkFailed();
                    
                    startBulk(thr, ts, fc, dwrefs, drrefs);
                    startRest(thr, ts, fc, dwrefs, drrefs);
                    
                    ts.unlock();
                    RefCount.decWriteRefs(dwrefs);
                    RefCount.decReadRefs(drrefs);
                    dwrefs = null;
                    drrefs = null;
                    it = null;
                    i++;
                default:
                    ts.waitFor();
            }
        }
        catch (Yield y) {
        	y.getState().push(drrefs);
            y.getState().push(dwrefs);
            y.getState().push(fc);
            y.getState().push(ts);
            y.getState().push(it);
            y.getState().push(i, 2);
            throw y;
        }
    }

	private boolean startBulk(LWThread thr, TPFThreadSet ts, int fcf, 
	        List<RefCount<PartialCloseable>> wrefs, List<RefCount<ReadRefWrapper>> rrefs) {
	    
	    int available = ts.freeSlots();
	    int j = 0;
	    Stack stack = thr.getStack();
	    for (; j < available && ts.hasNext(); j++) {
	        if (startOne(thr, ts, ts.next(), fcf, wrefs, rrefs)) {
                // aborted
                return true;
            }
        }
        return false;
    }
	
	private boolean startRest(LWThread thr, TPFThreadSet ts, int fcf, 
	        List<RefCount<PartialCloseable>> wrefs, List<RefCount<ReadRefWrapper>> rrefs) {
	    
        Stack stack = thr.getStack();
        while (ts.hasNext()) {
            ts.waitForSlot();
            if (startOne(thr, ts, ts.next(), fcf, wrefs, rrefs)) {
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

    private boolean startOne(final LWThread thr, final ThreadSet ts, final List<?> value, 
            final int fcf, List<RefCount<PartialCloseable>> wrefs, List<RefCount<ReadRefWrapper>> rrefs) {
        
        RefCount.incWriteRefs(wrefs);
        RefCount.incReadRefs(rrefs);
        LWThread ct = thr.fork(new KRunnable() {
            @Override
            public void run(LWThread thr2) {
                int i = thr2.checkSliceAndPopState(1);
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
                        default:
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
                    y.getState().push(i, 1);
                    throw y;
                }
            }
        });
        if(ts.add(ct)) {
            return true;
        }
        
        Stack cs = ct.getStack();
        cs.enter(this, frameSize);
        if (kvarRef != null) {
            kvarRef.setValue(cs, new RootClosedPrimitiveDataNode(indexVarField, value.get(0)));
        }
        vvarRef.setValue(cs, (DSHandle) value.get(1));
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
	    protected Iterator<List<?>> it;
	    
	    public TPFThreadSet(Iterator<List<?>> it, int maxThreads) {
	        super(maxThreads);
	        this.it = it;
	    }

        public boolean hasNext() {
            return it.hasNext();
        }
        
        public List<?> next() {
            return it.next();
        }
	}
	
	private static class TPFSCThreadSet extends TPFThreadSet implements FutureListener {
	    private Helper helper;
	    private boolean closed;
	    
	    public TPFSCThreadSet(Iterator<List<?>> it, int maxThreads) {
	        super(it, maxThreads);
	        helper = new Helper();
	    }
	    
	    public synchronized boolean hasNext() {
	        if (closed) {
	            return false;
	        }
	        if (it == null) {
	        	return false;
	        }
	        try {
	            boolean itHasNext = it.hasNext();
	            if (!itHasNext) {
	            	it = null;
	            }
	            return itHasNext;
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
