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
 * Created on Mar 11, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.griphyn.vdl.mapping.nodes.PartialCloseable;
import org.griphyn.vdl.mapping.nodes.ReadRefWrapper;

class RefCount<T> {
    public final T var;
    public final int count;

    public RefCount(T var, int count) {
        this.var = var;
        this.count = count;
    }
    
    public static <S> List<RefCount<S>> build(Stack stack, List<StaticRefCount<S>> srefs) {
        if (srefs == null) {
            return null;
        }
        List<RefCount<S>> l = new ArrayList<RefCount<S>>(srefs.size());
        for (StaticRefCount<S> s : srefs) {
            l.add(new RefCount<S>(s.ref.getValue(stack), s.count));
        }
        return l;
    }
    
    public static void decWriteRefs(List<RefCount<PartialCloseable>> rcs) throws ExecutionException {
            if (rcs != null) {
                for (RefCount<PartialCloseable> rc : rcs) {
                    rc.var.updateWriteRefCount(-rc.count);
                }
            }
        }

    public static void incWriteRefs(List<RefCount<PartialCloseable>> rcs) throws ExecutionException {
        if (rcs != null) {
            for (RefCount<PartialCloseable> rc : rcs) {
                rc.var.updateWriteRefCount(rc.count);
            }
        }
    }
    
    public static void decReadRefs(List<RefCount<ReadRefWrapper>> rcs) throws ExecutionException {
            if (rcs != null) {
                for (RefCount<ReadRefWrapper> rc : rcs) {
                    rc.var.updateReadRefCount(-rc.count);
                }
            }
        }

    public static void incReadRefs(List<RefCount<ReadRefWrapper>> rcs) throws ExecutionException {
        if (rcs != null) {
            for (RefCount<ReadRefWrapper> rc : rcs) {
                rc.var.updateReadRefCount(rc.count);
            }
        }
    }
}