/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class FuturePrimitiveDataNode extends AbstractFutureNonCompositeDataNode {
    private RootHandle root;
    private AbstractDataNode parent;
    
    protected FuturePrimitiveDataNode(Field field, RootHandle root, AbstractDataNode parent) {
        super(field);
        this.root = root;
        this.parent = parent;
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
        return calculatePathFromRoot();
    }
    
    @Override
    protected void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException {
    }

    @Override
    public void getLeaves(List<DSHandle> list) throws HandleOpenException {
        list.add(this);
    }
    
    @Override
    protected void clean0() {
        super.clean0();
        root = null;
        parent = null;
    }
}
