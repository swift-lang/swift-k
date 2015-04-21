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

import org.griphyn.vdl.type.impl.TypeImpl;
import org.griphyn.vdl.type.impl.TypeImpl.Array;

public class Types {
    
    public static final String DEFAULT_ARRAY_KEY_TYPE_NAME = "int";

	//TODO: check namespace references in type definitions
	private Map<String, Type> types;
	private Types prev;
	
	public Types() {
	    types = new HashMap<String, Type>();
	}
	
	public Types(Types prev) {
	    this();
	    this.prev = prev;
	}
	
	private String[] getArrayInnerTypeNames(String name) {
	    if(name.endsWith("]")) {
            int index = name.lastIndexOf('[');
            if (index == -1) {
                throw new RuntimeException("Malformed type name: " + name);
            }
            String keyTypeName = name.substring(index + 1, name.length() - 1);
            String baseTypeName = name.substring(0, index);
            if (keyTypeName.equals("")) {
                keyTypeName = DEFAULT_ARRAY_KEY_TYPE_NAME;
            }
            return new String[] {baseTypeName, keyTypeName};
        }
	    else {
	        return null;
	    }
	}
	
	private String[] getArrayOuterTypeNames(String name) {
	    int index = name.indexOf('[');
	    if (index >= 0) {
	        int i2 = name.indexOf(']');
	        if (i2 < index) {
	            throw new RuntimeException("Malformed type name: " + name);
	        }
	        // if a: tv[tk1][tk2]
	        // then a[k1] is a tv[tk2].
	        String keyTypeName = name.substring(index + 1, i2);
	        String baseTypeName = name.substring(0, index) + name.substring(i2 + 1);
	        if (keyTypeName.equals("")) {
	            keyTypeName = DEFAULT_ARRAY_KEY_TYPE_NAME;
	        }
	        return new String[] {baseTypeName, keyTypeName};
	    }
	    else {
	        return null;
	    }
    }
	
	private String getArrayInnerComponentTypeName(String name, int index) {
	    String[] names = getArrayInnerTypeNames(name);
        if (names == null) {
            return null;
        }
        else {
            return names[index];
        }
	}
	
	private String getArrayOuterComponentTypeName(String name, int index) {
        String[] names = getArrayOuterTypeNames(name);
        if (names == null) {
            return null;
        }
        else {
            return names[index];
        }
    }
		
	public synchronized Type getType(String name) throws NoSuchTypeException {
		Type type = types.get(name);
		if (type == null) {
		    String[] names = getArrayOuterTypeNames(name);
		    if (names != null) {
				Type baseType = getType(names[0]);
				Type keyType = getType(names[1]);
				if (!keyType.isPrimitive()) {
				    throw new NoSuchTypeException("Array key type must be a primitive type");
				}
				Type arrayType = new Array(baseType, keyType);
				addType(arrayType);
				return arrayType;
			}
			else if (prev != null) {
			    return prev.getType(name);
			}
			else {   
                throw new NoSuchTypeException(name);
            }
		}
		else {
			return type;
		}
	}
		
	//TODO: check duplicate type?
	public synchronized void addType(Type type) {
		types.put(type.getName(), type);
	}

	private Type addPrimitiveType(String name) {
		Type type = Type.Factory.createType(name, true);
		addType(type);
		return type;
	}
	
	public static final Types BUILT_IN_TYPES = new Types();

	public static final Type INT, STRING, FLOAT, BOOLEAN, ANY, EXTERNAL, AUTO;

	// add built-in primitive types
	static {
		STRING = BUILT_IN_TYPES.addPrimitiveType("string");
		INT = BUILT_IN_TYPES.addPrimitiveType("int");
		FLOAT = BUILT_IN_TYPES.addPrimitiveType("float");
		BOOLEAN = BUILT_IN_TYPES.addPrimitiveType("boolean");
		ANY = new TypeImpl("any", true) {
            @Override
            public boolean canBeAssignedTo(Type type) {
                return type.equals(this);
            }

            @Override
            public boolean isAssignableFrom(Type type) {
                return true;
            }
        }; 
		BUILT_IN_TYPES.addType(ANY);
		EXTERNAL = BUILT_IN_TYPES.addPrimitiveType("external");
		AUTO = BUILT_IN_TYPES.addPrimitiveType("auto");
	}

	public synchronized void resolveTypes() throws NoSuchTypeException {
		Map<String, Type> typesCopy = new HashMap<String, Type>(types);
		for (Map.Entry<String, Type> e : typesCopy.entrySet()) {
			Type type = e.getValue();
			for (Field field : type.getFields()) {
				Type resolved = getType(field.getType().getName());
				field.setType(resolved);
			}
		}
	}
}
