//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 18, 2014
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public abstract class AbstractSingleValuedSwiftFunction extends AbstractSingleValuedFunction {

    @Override
    public void runBody(LWThread thr) {
        Stack stack = thr.getStack();
        try {
            ret(stack, function(stack));
        }
        catch (DependentException e) {
            RootDataNode rdn = new RootDataNode(getReturnType());
            rdn.setValue(new DataDependentException(rdn, e));
            ret(stack, rdn);
        }
    }
    
    protected Type getReturnType() {
        return Types.ANY;
    }
}
