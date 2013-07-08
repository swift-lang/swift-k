/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 11, 2007
 */
package org.griphyn.vdl.mapping;

/**
 * Signifies a problem with data/mapping dependencies
 */
public abstract class DependentException extends RuntimeException {
	private DSHandle handle;

	public DependentException(DSHandle handle, Exception prev) {
		super(prev);
		this.handle = handle;
	}

	public DependentException(DSHandle handle) {
		super();
		this.handle = handle;
	}
	
	public abstract String getMessage();

	public DSHandle getHandle() {
		return handle;
	}
	
	public String getVariableInfo() {
        if (handle instanceof AbstractDataNode) {
            AbstractDataNode n = (AbstractDataNode) handle;
            return n.getDisplayableName() + ", line " + n.getDeclarationLine();
        }
        else {
            return handle.toString();
        }
    }

	public String toString() {
		return getMessage();
	}
}
