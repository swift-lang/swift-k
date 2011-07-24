package org.griphyn.vdl.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.type.impl.TypeImpl.Array;

public abstract class Types {

	//TODO: check namespace references in type definitions
	private static Map<String, Type> types = 
	    new HashMap<String, Type>();
	
	public synchronized static boolean isValidType(String name, Set<String> validTypes) {
	    if (validTypes.contains(name) || types.containsKey(name)) {
	        return true;
	    }
	    else {
	        String[] names = getArrayTypeNames(name);
	        if (names == null) {
	            return false;
	        }
	        else {
	            return isValidType(names[0], validTypes) && isValidType(names[1], validTypes);
	        }
	    }
	}
	
	private static String[] getArrayTypeNames(String name) {
	    if(name.endsWith("]")) {
            int index = name.lastIndexOf('[');
            if (index == -1) {
                throw new RuntimeException("Malformed type name: " + name);
            }
            String keyTypeName = name.substring(index + 1, name.length() - 1);
            String baseTypeName = name.substring(0, index);
            if (keyTypeName.equals("")) {
                keyTypeName = "int";
            }
            return new String[] {baseTypeName, keyTypeName};
        }
	    else {
	        return null;
	    }
	}
	
	private static String getArrayComponentTypeName(String name, int index) {
	    String[] names = getArrayTypeNames(name);
        if (names == null) {
            return null;
        }
        else {
            return names[index];
        }
	}
	
	public static String normalize(String t) {
        String[] names = getArrayTypeNames(t);
        if (names == null) {
            return t;
        }
        else {
            return normalize(names[0]) + "[" + names[1] + "]";
        }
    }
	
	public static String getArrayItemTypeName(String name) {
	    return getArrayComponentTypeName(name, 0);
	}
	
	public static String getArrayIndexTypeName(String name) {
        return getArrayComponentTypeName(name, 1);
    }

	public synchronized static Type getType(String name) throws NoSuchTypeException {
		Type type = types.get(name);
		if (type == null) {
		    String[] names = getArrayTypeNames(name);
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
