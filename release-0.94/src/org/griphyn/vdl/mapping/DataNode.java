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
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import org.griphyn.vdl.type.Field;


public class DataNode extends AbstractDataNode {
	private DSHandle root;
	private DSHandle parent;
	
	protected DataNode(Field field, DSHandle root, DSHandle parent) {
		super(field);
		if (parent != null && field.getId() == null) {
		    throw new IllegalArgumentException("Internal error: field id is null");
		}
		this.root = root;
		this.parent = parent;
	}
	
	public DSHandle getRoot() {
		return root;
	}
	
	public DSHandle getParent() {
		return parent;
	}
	
	public void setParent(DSHandle parent) {
		this.parent = parent;
	}

	public String getParam(MappingParam p) {
		return null;
	}
}
