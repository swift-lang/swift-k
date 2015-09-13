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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import k.rt.FutureListener;
import k.thr.Yield;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.mapping.OOBYield;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;

public abstract class AbstractFutureDataNode extends AbstractDataNode {
	private volatile boolean closed;
	private List<FutureListener> listeners;
	private AtomicInteger writeRefCount;

	public AbstractFutureDataNode(Field field) {
	    super(field);
    }
	
	@Override
    protected boolean addListener0(FutureListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<FutureListener>();
        }
        this.listeners.add(l);
        return this.isClosed();
    }
	
	@Override
	protected void notifyListeners() {
        List<FutureListener> l;
        synchronized(this) {
            l = this.listeners;
            this.listeners = null;
        }
        if (l != null) {
            for (FutureListener ll : l) {
                ll.futureUpdated(this);
                WaitingThreadsMonitor.removeThread(ll);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
    
    protected void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public void closeShallow() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        postCloseActions();
    }
    
    public void closeDeep() {
        closeShallow();
    }
    
    public void setValue(Object value) {
        throw new IllegalArgumentException(this.getFullName() 
            + " cannot set the value of a " + this.getType());
    }
    
    @Override
    protected void clean0() {
        listeners = null;
        super.clean0();
    }
    
    @Override
    public synchronized void setWriteRefCount(int count) {
        this.writeRefCount = new AtomicInteger(count);
    }

    @Override
    public int updateWriteRefCount(int delta) {
        int count = this.writeRefCount.addAndGet(delta);
               
        if (count < 0) {
            throw new IllegalArgumentException("Write reference count mismatch for " + this + ". Count is " + count);
        }
                        
        if (logger.isDebugEnabled()) {
            logger.debug(this + " writeRefCount " + count);
        }
        if (variableTracer.isEnabled()) {
            RootHandle root = getRoot();
            variableTracer.trace(root.getThread(), root.getLine(), getDisplayableName() + " WRITE_REF_COUNT " + delta + " -> " + count);
        }
        if (count == 0) {
            if (variableTracer.isEnabled()) {
                RootHandle root = getRoot();
                variableTracer.trace(root.getThread(), root.getLine(), getDisplayableName() + " CLOSE write ref count is zero");
            }
            closeDeep();
            this.writeRefCount = null;
        }
        return count;
    }
    
    @Override
    public synchronized void waitFor(Node who) {
        if (!closed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for " + this);
            }
            
            Yield y = new FutureNotYetAvailable(this);
            y.getState().addTraceElement(who);
            throw y;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Do not need to wait for " + this);
            }
            checkNoValue();
        }
    }
    
    @Override
    public synchronized void waitFor() throws OOBYield {
        if (!closed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for " + this);
            }
            
            throw new OOBYield(new FutureNotYetAvailable(this), this);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Do not need to wait for " + this);
            }
            checkNoValue();
        }
    }
    
    protected abstract void checkNoValue();
}
