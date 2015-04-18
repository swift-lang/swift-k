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

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.griphyn.vdl.type.impl.TypeImpl;

public interface Type {
	/**
	 * set the name of the type
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * set the namespace of the type
	 * 
	 * @param namespace
	 */
	public void setNamespace(String namespace);

	/**
	 * set the namespace of the type
	 * 
	 * @param namespace
	 *            URI of the namespace
	 */
	public void setNamespace(URI namespace);

	/**
	 * get the name of the type
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * get the namespace of the type
	 * 
	 * @return string representation of the namespace
	 */
	public String getNamespace();

	/**
	 * get the namespace of the type
	 * 
	 * @return URI of the namespace
	 */
	public URI getNamespaceURI();

	boolean isArray();
		
	/**
	 * Returns true if this type is a composite type (array or struct).
	 */
	boolean isComposite();

	/**
	 * set the full name as a QName
	 * 
	 * @return
	 */
	public void setQName(QName name);

	/**
	 * get the full name as a QName
	 * 
	 * @return
	 */
	public QName getQName();

	/**
	 * add a field to this type
	 * 
	 * @param field
	 * @throws DuplicateFieldException
	 */
	public void addField(Field field) throws DuplicateFieldException;

	/**
	 * add a primitive field to the type
	 * 
	 * @throws DuplicateFieldException
	 */
	public void addField(String name, Type type) throws DuplicateFieldException;

	/**
	 * get a field in the type by name
	 * 
	 * @param name
	 * @return Field
	 * @throws NoSuchFieldException
	 */
	public Field getField(String name) throws NoSuchFieldException;
	
	public int getFieldIndex(String name) throws NoSuchFieldException;

	/**
	 * get a list of field names
	 * 
	 * @return a list of strings
	 */
	public List<String> getFieldNames();

	/**
	 * get all the fields in this type
	 * 
	 * @return a list of Fields
	 */
	public List<Field> getFields();

	/**
	 * get the base type of this type as a type can be derived from another type
	 * 
	 * @return
	 */
	public Type getBaseType();

	/**
	 * set the base type of this type
	 */
	public void setBaseType(Type base);

	/**
	 * check if this is a primitive type
	 */
	public boolean isPrimitive();

	/**
	 * set this to be a primitive type
	 */
	public void setPrimitive();

	/**
	 * This method returns the type of the array consisting of elements of this
	 * type and integer keys.
	 */
	Type arrayType();
	
	Type arrayType(Type keyType);
	
	/** 
	 * This method, if invoked on an array type, returns the type of each item.
	 * If t is a type, t == t.arrayType().itemType();
	 */
	Type itemType();
	
	/**
	 * For an array, this returns the array key type
	 */
	Type keyType();

	/**
	 * A factory class with static methods for creating instances of Type.
	 */

	public static final class Factory {
		// TODO: should load TypeImpl instead
		// TODO: is this factory necessary?
		public static Type newInstance() {
			return new TypeImpl();
		}

		public static Type createType(String name, boolean primitive) {
			return new TypeImpl(name, primitive);
		}

		public static Type createType(String namespace, String name, boolean primitive) {
			return new TypeImpl(namespace, name, primitive);
		}

	}

    public boolean hasMappedComponents();
    
    public boolean hasArrayComponents();

    public boolean canBeAssignedTo(Type type);

    public boolean isAssignableFrom(Type type);
}
