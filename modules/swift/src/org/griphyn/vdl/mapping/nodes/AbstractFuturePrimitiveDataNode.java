//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.List;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Field;

public abstract class AbstractFuturePrimitiveDataNode extends AbstractFutureNonCompositeDataNode {
    
    public AbstractFuturePrimitiveDataNode(Field field) {
        super(field);
    }

    public void getFringePaths(List<Path> list, Path parentPath) throws HandleOpenException {
        // only mappable paths
    }
    
    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
        // only mappable paths
    }
}
