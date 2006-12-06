package org.griphyn.vdl.type.impl;

import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class FieldImpl implements Field {
	private String name = null;
	private Type type = null;
	private boolean array = false;

	public FieldImpl() {
	}

	public FieldImpl(String name, Type type, boolean array) {
		this.name = name;
		this.type = type;
		this.array = array;
	}
	
	public FieldImpl(String name, Type type) {
		this.name = name;
		this.type = type;
		this.array = false;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
