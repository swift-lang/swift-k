package org.griphyn.vdl.type.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class TypeImpl extends UnresolvedType {
	private boolean primitive;
	private Map fields;
	private Type baseType, arrayType;

	public TypeImpl() {
		this((URI) null, null, false, false);
	}

	public TypeImpl(String namespace, String name, boolean primitive, boolean array) {
		super(namespace, name, array);
		init(primitive, array);
	}

	public TypeImpl(URI namespace, String name, boolean primitive, boolean array) {
		super(namespace, name, array);
		init(primitive, array);
	}

	private void init(boolean primitive, boolean array) {
		this.primitive = primitive;
		if (!array) {
			this.arrayType = new Array(this, getNamespaceURI(), getName());
		}
		fields = new HashMap();
		baseType = null;
	}

	public TypeImpl(String name, boolean primitive, boolean array) {
		this((URI) null, name, primitive, array);
	}

	public TypeImpl(String name) {
		this(name, false, false);
	}

	public void addField(Field field) throws DuplicateFieldException {
		String name = field.getName();
		if (name == null)
			return;
		if (fields.get(name) != null) {
			throw new DuplicateFieldException(name);
		}
		else {
			fields.put(name, field);
		}
	}

	public void addField(String name, Type type) throws DuplicateFieldException {
		Field field = Field.Factory.createField(name, type);
		addField(field);
	}

	public Field getField(String name) throws NoSuchFieldException {
		if (name == null) {
			throw new NullPointerException();
		}

		Field field = (Field) fields.get(name);
		if (field != null) {
			return field;
		}
		else {
			throw new NoSuchFieldException(name);
		}
	}

	public List getFieldNames() {
		ArrayList list = new ArrayList();
		list.addAll(fields.keySet());
		return list;
	}

	public List getFields() {
		return new ArrayList(fields.values());
	}

	public Type getBaseType() {
		return baseType;
	}

	public Type arrayType() {
		if (isArray()) {
			return this;
		}
		else {
			return arrayType;
		}
	}

	public Type itemType() {
		return this;
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public void setBaseType(Type base) {
		baseType = base;
	}

	public void setPrimitive() {
		primitive = true;
	}

	private static class Array extends TypeImpl {
		private Field field;

		public Array(Type type, URI namespace, String name) {
			super(namespace, name, false, true);
			field = Field.Factory.newInstance();
			field.setType(type);
		}

		public Type arrayType() {
			return this;
		}

		public boolean isArray() {
			return true;
		}

		public Field getField(String name) throws NoSuchFieldException {
			return field;
		}

		public Type itemType() {
			return field.getType();
		}
	}
}
