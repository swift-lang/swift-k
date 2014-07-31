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

import org.griphyn.vdl.mapping.DSHandle;

class RefCount {
    public final DSHandle var;
    public final int count;

    public RefCount(DSHandle var, int count) {
        this.var = var;
        this.count = count;
    }
    
    public static List<RefCount> build(Stack stack, List<StaticRefCount> srefs) {
        if (srefs == null) {
            return null;
        }
        List<RefCount> l = new ArrayList<RefCount>(srefs.size());
        for (StaticRefCount s : srefs) {
            l.add(new RefCount((DSHandle) s.ref.getValue(stack), s.count));
        }
        return l;
    }
    
    public static void decRefs(List<RefCount> rcs) throws ExecutionException {
            if (rcs != null) {
                for (RefCount rc : rcs) {
                    rc.var.updateWriteRefCount(-rc.count);
                }
            }
        }

    public static void incRefs(List<RefCount> rcs) throws ExecutionException {
        if (rcs != null) {
            for (RefCount rc : rcs) {
                rc.var.updateWriteRefCount(rc.count);
            }
        }
    }
}