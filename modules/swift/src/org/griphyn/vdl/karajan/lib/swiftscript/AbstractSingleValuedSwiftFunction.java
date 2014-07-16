//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 17, 2014
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public abstract class AbstractSingleValuedSwiftFunction extends AbstractSingleValuedFunction {

    
    @Override
    public void runBody(LWThread thr) {
        Stack stack = thr.getStack();
        try {
            ret(stack, function(stack));
        }
        catch (DependentException e) {
            ret(stack, NodeFactory.newRoot(getFieldType(), e));
        }
    }
    
    protected Field getFieldType() {
        return Field.GENERIC_ANY;
    }
}
