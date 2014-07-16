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


package org.griphyn.vdl.type;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.type.impl.FieldImpl;
import org.griphyn.vdl.type.impl.ImmutableField;

public interface Field {
	/**
	 * get the name of the field
	 * @return
	 */
	public Comparable<?> getId();
	
	/**
	 * set the name of the field
	 *
	 */
	public void setId(Comparable<?> id);
		
	/**
	 * get the type of the field
	 * @return
	 */
	public Type getType();
	
	/**
	 * set the type of the field
	 * @param type
	 */
	public void setType(Type type);
	
	

	/**
	 * A factory class with static methods for creating instances
	 * of Field.
	 */

	public static final class Factory {
	    private static final Map<Field, Field> fieldCache = new HashMap<Field, Field>(); 
		public static Field newInstance() {
			return new FieldImpl();
		}
		
		public static Field createField(Comparable<?> id, Type type) {
			return new FieldImpl(id, type);
		}
		
		public static synchronized Field getImmutableField(Comparable<?> id, Type type) {
		    Field f = new ImmutableField(id, type);
		    Field cached = fieldCache.get(f);
		    if (cached == null) {
		        fieldCache.put(f, f);
		        return f;
		    }
		    else {
		        return cached;
		    }
		}
	}
	
	public static final Field GENERIC_STRING = new FieldImpl("?", Types.STRING);
	public static final Field GENERIC_ANY = new FieldImpl("?", Types.ANY);
	public static final Field GENERIC_BOOLEAN = new FieldImpl("?", Types.BOOLEAN);
	public static final Field GENERIC_INT = new FieldImpl("?", Types.INT);
	public static final Field GENERIC_FLOAT = new FieldImpl("?", Types.FLOAT);
	
	public static final Field GENERIC_STRING_ARRAY = new FieldImpl("?", Types.STRING.arrayType());
}

