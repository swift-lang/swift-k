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
 * Created on Sep 5, 2015
 */
package org.griphyn.vdl.mapping.nodes;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.RootHandle;

public class ReadRefWrapper implements PartialCloseable {
    public static final Logger logger = Logger.getLogger(ReadRefWrapper.class);
    protected static final Tracer variableTracer = Tracer.getTracer("VARIABLE");
    
    private AbstractDataNode h;
    private int count;
    
    public ReadRefWrapper(AbstractDataNode h, int readRefCount) {
        this.h = h;
        this.count = readRefCount;
    }

    public AbstractDataNode getHandle() {
        return h;
    }
    
    @Override
    public void fail(DependentException e) {
        h.fail(e);
    }

    @Override
    public int updateWriteRefCount(int delta) {
        int writeCount = h.updateWriteRefCount(delta);
        updateReadRefCount(delta);
        return writeCount;
    }

    public int decRefCount() {
        return modCount(-1);
    }

    public int decRefCount(int amount) {
        return modCount(-amount);
    }
    
    public int updateReadRefCount(int delta) {
        return modCount(delta);
    }

    private int modCount(int delta) {
        int newCount;
        AbstractDataNode localH;
        synchronized(this) {
            localH = h;
            count += delta;
            newCount = count;
        }
        //RootHandle root = localH.getRoot();
        //System.err.println();
        //System.err.println(h.getDisplayableName() + " READ_REF_COUNT " + delta + " -> " + newCount);
        //new Throwable().printStackTrace();
        if (shouldLog()) {
            log(localH, newCount, -1);
        }
        if (newCount < 1) {
            if (newCount < 0) {
                throw new IllegalArgumentException("Read reference count mismatch for " + localH + ". Count is " + newCount);
            }
            if (newCount == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Soft cleaning " + localH.getDisplayableName());
                }
                h = null;
            }
        }
        return newCount;
    }

    private static void log(AbstractDataNode var, int newCount, int delta) {
        if (logger.isDebugEnabled()) {
            logger.debug(var + " writeRefCount " + newCount);
        }
        if (variableTracer.isEnabled()) {
            RootHandle root = var.getRoot();
            variableTracer.trace(root.getThread(), root.getLine(), var.getDisplayableName() + " READ_REF_COUNT " + delta + " -> " + newCount);
        }
        if (newCount == 0) {
            if (variableTracer.isEnabled()) {
                RootHandle root = var.getRoot();
                variableTracer.trace(root.getThread(), root.getLine(), var.getDisplayableName() + " CLEAN read ref count is zero");
            }
        }
    }
    
    private static boolean shouldLog() {
        return logger.isDebugEnabled() || variableTracer.isEnabled();
    }
}
