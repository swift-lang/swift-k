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


package org.griphyn.vdl.type.impl;

import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class FieldImpl implements Field {
	private Comparable<?> id;
	private Type type;
	private boolean array = false;

	public FieldImpl() {
	}

	public FieldImpl(Comparable<?> id, Type type, boolean array) {
		this.id = id;
		this.type = type;
		this.array = array;
	}
	
	public FieldImpl(Comparable<?> id, Type type) {
		this.id = id;
		this.type = type;
		this.array = false;
	}
	
	public Comparable<?> getId() {
		return id;
	}

	public void setId(Comparable<?> id) {
		this.id = id;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray() {
		array = true;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
