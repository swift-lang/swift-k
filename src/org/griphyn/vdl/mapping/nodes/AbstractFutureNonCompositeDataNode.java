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

import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.MissingDataException;
import org.griphyn.vdl.type.Field;


public abstract class AbstractFutureNonCompositeDataNode extends AbstractFutureDataNode {
    private Object value;

    public AbstractFutureNonCompositeDataNode(Field field) {
        super(field);
    }

    @Override
    protected Object getRawValue() {
        return value;
    }
    
    protected void checkDataException() {
        if (value instanceof DependentException) {
            throw (DependentException) value;
        }
    }

    protected void checkMappingException() {
        if (value instanceof MappingDependentException) {
            throw (MappingDependentException) value;
        }
    }

    @Override
    public synchronized Object getValue() {
        checkNoValue();
        checkDataException();
        return value;
    }
    
    @Override
    protected void checkNoValue() {
        if (value == null) {
            AbstractDataNode parent = getParentNode();
            if (parent != null && parent.getType().isArray()) {
                throw new IndexOutOfBoundsException("Invalid index [" + field.getId() + "] for " + 
                    parent.getFullName());
            }
            else if (getType().isPrimitive()) {
                throw new RuntimeException(getFullName() + " has no value");
            }
            else {
                throw new MissingDataException(this, map());
            }
        }
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        }
    }
    
    public void setValue(Object value) {
        synchronized(this) {
            if (isClosed()) {
                throw new IllegalArgumentException(this.getFullName() 
                        + " is closed with a value of " + this.value);
            }
            if (this.value != null) {
                throw new IllegalArgumentException(this.getFullName() 
                        + " is already assigned with a value of " + this.value);
            }
        
            this.value = value;
            this.setClosed(true);
        }
        postCloseActions();
    }
    
    public boolean isArray() {
        return false;
    }
    
    @Override
    protected void clean0() {
        value = null;
        super.clean0();
    }
    
    @Override
    public void closeArraySizes() {
    }
}
