//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 4, 2013
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.compiled.nodes.functions.UnaryOp;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public abstract class SwiftUnaryOp extends UnaryOp<AbstractDataNode, DSHandle> {

    @Override
    public DSHandle function(Stack stack) {
        try {
        	AbstractDataNode v1 = this.v1.getValue(stack);
        	v1.waitFor(this);
            return value(v1);
        }
        catch (DependentException e) {
            return NodeFactory.newRoot(getReturnType(), e);
        }
    }
    
    protected Field getReturnType() {
        return Field.GENERIC_ANY; 
    }
}
