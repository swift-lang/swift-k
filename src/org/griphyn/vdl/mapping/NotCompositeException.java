//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 12, 2013
 */
package org.griphyn.vdl.mapping;

import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class NotCompositeException extends RuntimeException {
    private AbstractDataNode node;

    public NotCompositeException(AbstractDataNode node) {
        this.node = node;
    }

    @Override
    public String getMessage() {
        return "Type '" + node.getType() + "' is not a composite type.";
    }
    
    public AbstractDataNode getDataNode() {
        return node;
    }
}
