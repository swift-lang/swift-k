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
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Field;

public abstract class AbstractFutureMappedSingleDataNode extends AbstractFutureNonCompositeDataNode {

    public AbstractFutureMappedSingleDataNode(Field field) {
        super(field);    
    }
    
    public void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
        list.add(myPath);
    }
    
    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
        list.add(this);
    }
    
    @Override
    protected void clean0() {
        Mapper mapper = getMapper();
        if (mapper != null) {
            mapper.clean(getPathFromRoot());
        }
        super.clean0();
    }
}
