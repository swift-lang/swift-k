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
