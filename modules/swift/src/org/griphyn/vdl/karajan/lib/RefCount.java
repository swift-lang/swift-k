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