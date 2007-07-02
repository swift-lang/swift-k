package org.griphyn.vdl.type;

import org.griphyn.vdl.type.impl.FieldImpl;

public interface Field {
	/**
	 * get the name of the field
	 * @return
	 */
	public String getName();
	
	/**
	 * set the name of the field
	 *
	 */
	public void setName(String name);
	
	/**
	 * check if the field is an array field
	 * @return
	 */
	public boolean isArray();
	
	/**
	 * set the field to be an array field
	 * @param array
	 */
	public void setArray();
	
	/**
	 * get the type of the field
	 * @return
	 */
	public Type getType();
	
	/**
	 * set the type of the field
	 * @param type
	 */
	public void setType(Type type);

	/**
	 * A factory class with static methods for creating instances
	 * of Field.
	 */

	public static final class Factory
	{
		public static Field newInstance() {
			return new FieldImpl();
		}
		
		public static Field createField(String name, Type type) {
			return new FieldImpl(name, type);
		}

		public static Field createField(String name, Type type, boolean array) {
			return new FieldImpl(name, type, array);
		}
	}
}

