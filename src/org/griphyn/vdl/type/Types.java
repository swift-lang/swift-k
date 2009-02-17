package org.griphyn.vdl.type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Types {

	//TODO: check namespace references in type definitions
	private static Map types = new HashMap();

	private String namespace = null;

	public synchronized static Type getType(String name) throws NoSuchTypeException {
		Type type = (Type) types.get(name);
		if (type == null) {
			if(name.endsWith("[]")) {
				String base = name.substring(0, name.length() - 2);
				Type baseType = getType(base);
				Type arrayType = baseType.arrayType();
				addType(arrayType);
				return arrayType;
			} else {
				throw new NoSuchTypeException(name);
			}
		}
		else {
			return type;
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

	public static final Type INT, STRING, FLOAT, BOOLEAN, ANY, EXTERNAL;

	// add built-in primitive types
	static {
		STRING = addPrimitiveType("string");
		INT = addPrimitiveType("int");
		FLOAT = addPrimitiveType("float");
		BOOLEAN = addPrimitiveType("boolean");
		ANY = addPrimitiveType("any");
		EXTERNAL = addPrimitiveType("external");
	}

	public synchronized static void resolveTypes() throws NoSuchTypeException {
		Map typesCopy = new HashMap(types);
		Iterator i = typesCopy.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			Type type = (Type) e.getValue();
			Iterator j = type.getFields().iterator();
			while (j.hasNext()) {
				Field field = (Field) j.next();
				Type resolved = getType(field.getType().getName());
				field.setType(resolved);
			}
		}
	}
}
