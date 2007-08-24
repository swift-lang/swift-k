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
	}
}

