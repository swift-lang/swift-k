//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2013
 */
package org.griphyn.vdl.mapping;

import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class MissingDataException extends RuntimeException {
    public MissingDataException(AbstractDataNode n, PhysicalFormat pf) {
        super("File not found for variable '" + n.getFullName() + "': " + pf);
    }
}
