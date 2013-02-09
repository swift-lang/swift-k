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
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public abstract class SwiftUnaryOp extends UnaryOp<AbstractDataNode, DSHandle> {

    @Override
    public DSHandle function(Stack stack) {
    	AbstractDataNode v1 = this.v1.getValue(stack);
    	v1.waitFor(this);
        return value(v1);
    }
}
