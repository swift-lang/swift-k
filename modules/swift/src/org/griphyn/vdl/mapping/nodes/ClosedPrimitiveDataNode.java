//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;

public class ClosedPrimitiveDataNode extends AbstractClosedMappedSingleDataNode {
    private RootHandle root;
    private AbstractDataNode parent;
    private Path pathFromRoot;
    
    protected ClosedPrimitiveDataNode(Field field, RootHandle root, AbstractDataNode parent, Object value) {
        super(field, value);
        this.root = root;
        this.parent = parent;
        this.pathFromRoot = parent.getPathFromRoot().addLast(field.getId(), parent.getType().isArray());
    }
    
    public RootHandle getRoot() {
        return root;
    }
    
    public DSHandle getParent() {
        return parent;
    }
    
    public AbstractDataNode getParentNode() {
        return parent;
    }
    
    @Override
    public Path getPathFromRoot() {
        return pathFromRoot;
    }
}
