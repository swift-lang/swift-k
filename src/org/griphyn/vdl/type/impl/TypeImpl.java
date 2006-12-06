package org.griphyn.vdl.type.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class TypeImpl implements Type {
	private String name;
	private String namespace;
	private boolean primitive;
	private Map fields;
	private Type baseType;
	
	public TypeImpl() {
		fields = new HashMap();
		baseType = null;
		primitive = false;
	}
	
	public TypeImpl(String namespace, String name, boolean primitive) {
		this();
		this.namespace = namespace;
		this.name = name;
		this.primitive = primitive;
	}
	
	public TypeImpl(String name, boolean primitive) {
		this(null, name, primitive);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setNamespace(URI namespace) {
		this.namespace = namespace.toString();
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public URI getNamespaceURI() {
		URI uri= null;
		try {
			uri =  new URI(namespace);
		}
		catch (URISyntaxException e) {
			// 
			e.printStackTrace();
		}
		return uri;
	}

	public void setQName(QName name) {
		this.name = name.getLocalPart();
		this.namespace = name.getNamespaceURI();
	}

	public QName getQName() {
		QName qn = new QName(namespace, name);
		return qn;
	}

	public void addField(Field field) throws DuplicateFieldException {
		String name = field.getName();
		if (name == null)
			return;
		if (fields.get(name) != null) {
			throw new DuplicateFieldException(name);
		} else 
			fields.put(name, field);
	}

	public void addPrimitiveField(String name, String type, boolean isArray) throws DuplicateFieldException {
		Type t = Type.Factory.createType(type, true);
		Field field = Field.Factory.createField(name, t, isArray);
		addField(field);
	}

	public void addPrimitiveField(String name, String type) throws DuplicateFieldException {
		addPrimitiveField(name, type, false);
	}

	public void addField(String name, String type, boolean isArray) throws DuplicateFieldException {
		// create a non-primitive type
		Type t = Type.Factory.createType(type, false);
		Field field = Field.Factory.createField(name, t, isArray);
		addField(field);
	}

	public void addField(String name, String type) throws DuplicateFieldException {
		addField(name, type, false);
	}

	public void addField(String name, QName type, boolean isArray) throws DuplicateFieldException {
		Type t = Type.Factory.createType(type.getNamespaceURI(), type.getLocalPart(), false);
		Field field = Field.Factory.createField(name, t, isArray);
		addField(field);
	}

	public void addField(String name, QName type) throws DuplicateFieldException {
		addField(name, type, false);
	}

	public void addField(String name, Type type, boolean isArray) throws DuplicateFieldException {
		Field field = Field.Factory.createField(name, type, isArray);
		addField(field);
	}

	public void addField(String name, Type type) throws DuplicateFieldException {
		addField(name, type, false);		
	}

	public Field getField(String name) throws NoSuchFieldException {
		if (name == null)
			return null;
		
		Field field = (Field) fields.get(name);
		if (field != null)
			return field;
		else
			throw new NoSuchFieldException(name);
	}

	public Type getFieldType(String name) throws NoSuchFieldException {
		Field field = getField(name);
		return field.getType();
	}

	public boolean isArrayField(String name) throws NoSuchFieldException {
		Field field = getField(name);
		return field.isArray();
	}

	public boolean isPrimitiveField(String name) throws NoSuchFieldException {
		Type type = getFieldType(name);
		return type.isPrimitive();
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

	public boolean isPrimitive() {
		return primitive;
	}

	public void setBaseType(Type base) {
		baseType = base;
	}

	public void setPrimitive() {
		primitive = true;
	}
}
