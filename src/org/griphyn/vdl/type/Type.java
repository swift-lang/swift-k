package org.griphyn.vdl.type;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.griphyn.vdl.type.impl.TypeImpl;

public interface Type {
	/**
	 * set the name of the type
	 * @param name
	 */
	public void setName(String name);

	/**
	 * set the namespace of the type
	 * @param namespace
	 */
	public void setNamespace(String namespace);

	/**
	 * set the namespace of the type
	 * @param namespace URI of the namespace
	 */
	public void setNamespace(URI namespace);

	/**
	 * get the name of the type
	 * @return
	 */
	public String getName();

	/**
	 * get the namespace of the type
	 * @return string representation of the namespace
	 */
	public String getNamespace();

	/**
	 * get the namespace of the type
	 * @return URI of the namespace
	 */
	public URI getNamespaceURI();

	/**
	 * set the full name as a QName
	 * @return
	 */
	public void setQName(QName name);

	/**
	 * get the full name as a QName
	 * @return
	 */
	public QName getQName();

	/**
	 * add a field to this type
	 * @param field
	 * @throws DuplicateFieldException 
	 */
	public void addField(Field field) throws DuplicateFieldException;

	/**
	 *  add a primitive field to the type
	 * @throws DuplicateFieldException 
	 */
	public void addPrimitiveField(String name, String type, boolean isArray) throws DuplicateFieldException;

	/**
	 * add a primitive non-array field
	 * @param name
	 * @param type
	 * @throws DuplicateFieldException 
	 */
	public void addPrimitiveField(String name, String type) throws DuplicateFieldException;

	/**
	 *  add a non-primitive field to the type
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, String type, boolean isArray) throws DuplicateFieldException;

	/**
	 * add a non-primitive non-array field
	 * @param name
	 * @param type
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, String type) throws DuplicateFieldException;

	/**
	 * add a field to the type
	 * @param name
	 * @param type
	 * @param isArray
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, QName type, boolean isArray) throws DuplicateFieldException;

	/**
	 * add a non-array field
	 * @param name
	 * @param type
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, QName type) throws DuplicateFieldException;

	/**
	 * add a field to the type
	 * @param name
	 * @param type
	 * @param isArray
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, Type type, boolean isArray) throws DuplicateFieldException;

	/**
	 * add a field to the type 
	 * @param name
	 * @param type
	 * @throws DuplicateFieldException 
	 */
	public void addField(String name, Type type) throws DuplicateFieldException;

	/**
	 * get a field in the type by name
	 * @param name
	 * @return Field
	 * @throws NoSuchFieldException 
	 */
	public Field getField(String name) throws NoSuchFieldException;

	/**
	 * get the type of a field
	 * @param name
	 * @return
	 * @throws NoSuchFieldException 
	 */
	public Type getFieldType(String name) throws NoSuchFieldException;

	/**
	 * check if a field is an array
	 * @param name
	 * @return true if it is array
	 * @throws NoSuchFieldException 
	 */
	public boolean isArrayField(String name) throws NoSuchFieldException;

	/**
	 * check if a field is of primitive type
	 * @param name
	 * @return true if primitive
	 * @throws NoSuchFieldException 
	 */
	public boolean isPrimitiveField(String name) throws NoSuchFieldException;

	/**
	 * get a list of field names
	 * @return a list of strings
	 */
	public List getFieldNames();

	/**
	 * get all the fields in this type
	 * @return a list of Fields
	 */
	public List getFields();

	/**
	 * get the base type of this type
	 * as a type can be derived from another type
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
	 * A factory class with static methods for creating instances
	 * of Type.
	 */

	public static final class Factory
	{
		//TODO: should load TypeImpl instead
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
}
