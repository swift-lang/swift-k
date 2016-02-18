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

import k.rt.FutureListener;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.OOBYield;
import org.griphyn.vdl.type.Field;

public abstract class AbstractClosedDataNode extends AbstractDataNode {

    public AbstractClosedDataNode(Field field) {
        super(field);
    }

    @Override
    public boolean isClosed() {
        return true;
    }
    
    @Override
    public void closeShallow() {
        // already closed
    }

    @Override
    public void closeDeep() {
        // already closed
    }
    
    public void setValue(Object value) {
        throw new IllegalArgumentException(this.getFullName() 
            + " is closed with a value of " + this.getValue());
    }

    @Override
    protected boolean addListener0(FutureListener l) {
        throw new UnsupportedOperationException("Cannot add listener to closed node");
    }

    @Override
    protected void notifyListeners() {
    }

    @Override
    public void setWriteRefCount(int count) {
        if (count != 0) {
            throw new UnsupportedOperationException("Attempt to set non-zero write ref count on read-only node");
        }
    }

    @Override
    public void fail(DependentException e) {
        // do nothing; this is already closed
    }

    @Override
    public int updateWriteRefCount(int delta) {
        throw new UnsupportedOperationException("Attempt to update write ref count on read-only node");
    }
    
    @Override
    public synchronized void waitFor(Node who) {
    }
    
    @Override
    public synchronized void waitFor() throws OOBYield {
    }

    @Override
    public void waitForAll(Node who) {
    }
}
