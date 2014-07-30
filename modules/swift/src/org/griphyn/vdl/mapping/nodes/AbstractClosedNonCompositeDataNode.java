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

import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.type.Field;

public abstract class AbstractClosedNonCompositeDataNode extends AbstractClosedDataNode {
    private Object value;
    
    public AbstractClosedNonCompositeDataNode(Field field, Object value) {
        super(field);
        this.value = value;
    }
    
    public AbstractClosedNonCompositeDataNode(Field field, DependentException e) {
        super(field);
        this.value = new DataDependentException(this, e);
    }

    @Override
    public synchronized Object getValue() {
        return value;
    }
    
    @Override
    protected Object getRawValue() {
        return value;
    }
        
    public boolean isArray() {
        return false;
    }
    
    @Override
    public void closeArraySizes() {
    }
}
