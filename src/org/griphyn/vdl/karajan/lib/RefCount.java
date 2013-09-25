//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 2, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.griphyn.vdl.mapping.DSHandle;

public class RefCount {
    public final DSHandle var;
    public final int count;
    
    public RefCount(DSHandle var, int count) {
        this.var = var;
        this.count = count;
    }
    
    public static List<RefCount> build(List<StaticRefCount> srefs, VariableStack stack) throws VariableNotFoundException {
        if (srefs == null) {
            return null;
        }
        List<RefCount> l = new ArrayList<RefCount>(srefs.size());
        for (StaticRefCount s : srefs) {
            l.add(new RefCount((DSHandle) stack.getVar(s.name), s.count));
        }
        return l;
    }
}