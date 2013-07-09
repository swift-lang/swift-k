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
 * Created on Jan 13, 2007
 */
package org.griphyn.vdl.mapping;

/**
 * Signals a problem with mapping dependencies. Both data and mapping
 * must not continue.
 */
public class MappingDependentException extends DependentException {

	public MappingDependentException(DSHandle handle, Exception prev) {
		super(handle, prev);
	}

	public MappingDependentException(DSHandle handle) {
		super(handle);	
	}

	public String getMessage() {
		return getVariableInfo() + " not mapped due to error in mapping dependencies";
	}
}
