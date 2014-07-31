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
import java.util.Set;

import org.griphyn.vdl.type.impl.TypeImpl.Array;

public abstract class Types {
    
    public static final String DEFAULT_ARRAY_KEY_TYPE_NAME = "int";

	//TODO: check namespace references in type definitions
	private static Map<String, Type> types = 
	    new HashMap<String, Type>();
	
	public synchronized static boolean isValidType(String name, Set<String> validTypes) {
	    if (validTypes.contains(name) || types.containsKey(name)) {
	        return true;
	    }
	    else {
	        String[] names = getArrayInnerTypeNames(name);
	        if (names == null) {
	            return false;
	        }
	        else {
	            return isValidType(names[0], validTypes) && isValidType(names[1], validTypes);
	        }
	    }
	}
	
	private static String[] getArrayInnerTypeNames(String name) {
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
	
	private static String[] getArrayOuterTypeNames(String name) {
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
	
	private static String getArrayInnerComponentTypeName(String name, int index) {
	    String[] names = getArrayInnerTypeNames(name);
        if (names == null) {
            return null;
        }
        else {
            return names[index];
        }
	}
	
	private static String getArrayOuterComponentTypeName(String name, int index) {
        String[] names = getArrayOuterTypeNames(name);
        if (names == null) {
            return null;
        }
        else {
            return names[index];
        }
    }
	
	public static String normalize(String t) {
        String[] names = getArrayInnerTypeNames(t);
        if (names == null) {
            return t;
        }
        else {
            return normalize(names[0]) + "[" + names[1] + "]";
        }
    }
	
	/**
     * Returns the inner item type name for an array. That is, if
     * a is of type t[tk1][tk2]...[tkN], then the inner item type name
     * is t[tk1][tk2]...[tk(N-1)]
     */
	public static String getArrayInnerItemTypeName(String name) {
	    return getArrayInnerComponentTypeName(name, 0);
	}
	
	/**
     * Returns the inner key type name for an array. That is, if
     * a is of type t[tk1][tk2]...[tkN], then the inner key type name
     * is tkN
     */
	public static String getArrayInnerIndexTypeName(String name) {
        return getArrayInnerComponentTypeName(name, 1);
    }
	
	/**
     * Returns the outer key type name for an array. That is, if
     * a is of type t[tk1][tk2]...[tkN], then the outer key type name
     * is tk1
     */
	public static String getArrayOuterIndexTypeName(String name) {
        return getArrayOuterComponentTypeName(name, 1);
    }
	
	/**
     * Returns the outer item type name for an array. That is, if
     * a is of type t[tk1][tk2]...[tkN], then the outer key type name
     * is t[tk2]...[tkN]
     */
    public static String getArrayOuterItemTypeName(String name) {
        return getArrayOuterComponentTypeName(name, 0);
    }

	public synchronized static Type getType(String name) throws NoSuchTypeException {
		Type type = types.get(name);
		if (type == null) {
		    String[] names = getArrayInnerTypeNames(name);
		    if (names != null) {
				Type baseType = getType(names[0]);
				Type keyType = getType(names[1]);
				Type arrayType = new Array(baseType, keyType);
				addType(arrayType);
				return arrayType;
			}
			else {
                throw new NoSuchTypeException(name);
            }
		}
		else {
			return type;
		}
	}
		
	public static boolean isPrimitive(String name) {
	    try {
	        Type t = getType(name);
	        return t.isPrimitive();
	    }
	    catch (NoSuchTypeException e) {
	        return false;
	    }
	}

	//TODO: check duplicate type?
	public synchronized static void addType(Type type) {
		types.put(type.getName(), type);
	}

	private static Type addPrimitiveType(String name) {
		Type type = Type.Factory.createType(name, true);
		addType(type);
		return type;
	}

	public static final Type INT, STRING, FLOAT, BOOLEAN, ANY, EXTERNAL, AUTO;

	// add built-in primitive types
	static {
		STRING = addPrimitiveType("string");
		INT = addPrimitiveType("int");
		FLOAT = addPrimitiveType("float");
		BOOLEAN = addPrimitiveType("boolean");
		ANY = addPrimitiveType("any");
		EXTERNAL = addPrimitiveType("external");
		AUTO = addPrimitiveType("auto");
	}

	public synchronized static void resolveTypes() throws NoSuchTypeException {
		Map<String, Type> typesCopy = 
		    new HashMap<String, Type>(types);
		for (Map.Entry<String, Type> e : typesCopy.entrySet()) {
			Type type = e.getValue();
			for (Field field : type.getFields()) {
				Type resolved = getType(field.getType().getName());
				field.setType(resolved);
			}
		}
	}
}
