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
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;

public class FutureMappedSingleDataNode extends AbstractFutureNonCompositeDataNode {
    private RootHandle root;
    private AbstractDataNode parent;
    private Path pathFromRoot;
    
    protected FutureMappedSingleDataNode(Field field, RootHandle root, AbstractDataNode parent) {
        super(field);
        this.root = root;
        this.parent = parent;
        this.pathFromRoot = calculatePathFromRoot();
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
    
    @Override
    protected void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
        list.add(myPath);
    }

    @Override
    protected void getLeaves(List<DSHandle> list) throws HandleOpenException {
        list.add(this);
    }
    
    @Override
    protected void clean0() {
        super.clean0();
        root = null;
        parent = null;
        pathFromRoot = null;
    }
}
