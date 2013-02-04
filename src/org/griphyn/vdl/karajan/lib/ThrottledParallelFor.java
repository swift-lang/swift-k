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
import java.util.StringTokenizer;

import k.rt.ExecutionException;
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
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.UParallelFor;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.util.VDL2Config;

public class ThrottledParallelFor extends UParallelFor {
	public static final Logger logger = Logger
			.getLogger(ThrottledParallelFor.class);
	
	public static final int DEFAULT_MAX_THREADS = 1024;

	private ArgRef<Boolean> selfClose;
	private ArgRef<String> refs;
	private ArgRef<String> _kvar;
	private ArgRef<String> _vvar;
	private ArgRef<String> _traceline;
	    
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

    private static class StaticRefCount {
        public final VarRef<?> ref;
        public final int count;

        public StaticRefCount(VarRef<?> ref, int count) {
            this.ref = ref;
            this.count = count;
        }
    }

    private static class RefCount {
        public final DSHandle var;
        public final int count;

        public RefCount(DSHandle var, int count) {
            this.var = var;
            this.count = count;
        }

        public void inc() {

        }

        public void dec() {

        }
    }
    
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        srefs = buildStaticRefs(scope);
        if (_traceline.getValue() != null) {
            setLine(Integer.parseInt(_traceline.getValue()));
        }
        forTracer = Tracer.getTracer(this, "FOREACH");
        iterationTracer = Tracer.getTracer(this, "ITERATION");
        return super.compileBody(w, argScope, scope);
    }
    
     private List<StaticRefCount> buildStaticRefs(Scope scope) {
        String refs = this.refs.getValue();
        if (refs == null) {
            return null;
        }
        List<StaticRefCount> l = new ArrayList<StaticRefCount>();
        String name = null;
        boolean flip = true;
        StringTokenizer st = new StringTokenizer(refs);
        while (st.hasMoreTokens()) {
            if (flip) {
                name = st.nextToken();
            }
            else {
                int count = Integer.parseInt(st.nextToken());
                l.add(new StaticRefCount(scope.getVarRef(name), count));
            }
            flip = !flip;
        }
        return l;
    }

    private List<RefCount> buildRefs(Stack stack) {
        if (srefs == null) {
            return null;
        }
        List<RefCount> l = new ArrayList<RefCount>(srefs.size());
        for (StaticRefCount s : srefs) {
            l.add(new RefCount((DSHandle) s.ref.getValue(stack), s.count));
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void runBody(final LWThread thr) {        
        int i = thr.checkSliceAndPopState();
        Iterator<Object> it = (Iterator<Object>) thr.popState();
        ThrottledThreadSet ts = (ThrottledThreadSet) thr.popState();
        int fc = thr.popIntState();
        List<RefCount> drefs = (List<RefCount>) thr.popState();
        Stack stack = thr.getStack();
        try {
            switch(i) {
                case 0:
                    it = in.getValue(stack).iterator();
                    ts = new ThrottledThreadSet(getMaxThreads());
                    drefs = buildRefs(stack);
                    ts.lock();
                    fc = stack.frameCount() + 1;
                    
                    if (forTracer.isEnabled()) {
                        forTracer.trace(thr);
                    }
                    
                    i++;
                case 1:
                    final ThreadSet tsf = ts;
                    
                    ts.checkFailed();
                    
                    startBulk(thr, ts, it, fc, drefs);
                    startRest(thr, ts, it, fc, drefs);
                    
                    ts.unlock();
                    decRefs(drefs);
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

	private boolean startBulk(LWThread thr, ThrottledThreadSet ts, Iterator<Object> it, int fcf, List<RefCount> refs) {
	    int available = ts.freeSlots();
	    int j = 0;
	    Stack stack = thr.getStack();
	    for (; j < available && it.hasNext(); j++) {
	        if (startOne(thr, ts, it.next(), fcf, refs)) {
                // aborted
                return true;
            }
        }
        return false;
    }
	
	private boolean startRest(LWThread thr, ThrottledThreadSet ts, Iterator<Object> it, int fcf, List<RefCount> refs) {
        Stack stack = thr.getStack();
        while (it.hasNext()) {
            ts.waitForSlot();
            if (startOne(thr, ts, it.next(), fcf, refs)) {
                return true;
            }
        }
        return false;
    }

    private boolean startOne(final LWThread thr, final ThreadSet ts, final Object value, final int fcf, List<RefCount> refs) {
        incRefs(refs);
        LWThread ct = thr.fork(new KRunnable() {
            @Override
            public void run(LWThread thr2) {
                try {
                    if (iterationTracer.isEnabled()) {
                        iterationTracer.trace(thr2, unwrap(value));
                    }

                    if (CompilerSettings.PERFORMANCE_COUNTERS) {
                        startCount++;
                    }
                    body.run(thr2);
                    ts.threadDone(thr2, null);
                }
                catch (ExecutionException e) {
                    throw e;
                }
                catch (Exception e) {
                    thr2.getStack().dropToFrame(fcf);
                    ts.threadDone(thr2, new ExecutionException(ThrottledParallelFor.this, e));
                    ts.abortAll();
                    thr.awake();
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

	
	private int getMaxThreads() {
	    if (maxThreadCount < 0) {
            try {
                maxThreadCount = TypeUtil.toInt(VDL2Config.getConfig()
                        .getProperty("foreach.max.threads", String.valueOf(DEFAULT_MAX_THREADS)));
            }
            catch (IOException e) {
                maxThreadCount = DEFAULT_MAX_THREADS;
            }
        }
	    return maxThreadCount;
	}
	
	protected Object unwrap(Object value) {
        if (value instanceof Pair) {
            Pair p = (Pair) value;
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
}
