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

	public FieldImpl() {
	}
	
	public FieldImpl(Comparable<?> id, Type type) {
		this.id = id;
		this.type = type;
	}
	
	public Comparable<?> getId() {
		return id;
	}

	public void setId(Comparable<?> id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode()) + (type == null ? 0 : type.hashCode());  
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Field) {
            Field f = (Field) obj;
            return comp(f.getId(), id) && comp(f.getType(), type);
        }
        return super.equals(obj);
    }

    private boolean comp(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }
    
    public String toString() {
        return id + ": " + type; 
    }
}
