//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 10, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.lang.reflect.Field;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;


public class IntrospectionHelper {

	public static void setField(WrapperNode w, Node owner, String fname, Object value) throws CompilationException {
		if (owner == null) {
			throw new NullPointerException("owner");
		}
		if (fname == null) {
			throw new NullPointerException("field name");
		}
	    try {
			Field f = findField(owner.getClass(), fname);
			f.setAccessible(true);
			f.set(owner, value);
		}
		catch (SecurityException e) {
			throw new CompilationException(w, "Cannot access field '" + fname + "' of class " + owner.getClass(), e);
		}
		catch (NoSuchFieldException e) {
			throw new CompilationException(w, "Invalid field '" + fname + "' for class " + owner.getClass(), e);
		}
		catch (IllegalArgumentException e) {
			throw new CompilationException(w, "Cannot set field '" + fname + "' of class " + owner.getClass(), e);
		}
		catch (IllegalAccessException e) {
			throw new CompilationException(w, "Cannot access field '" + fname + "' of class " + owner.getClass(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Field findField(Class<? extends Node> cls, String name) throws NoSuchFieldException {
		while (true) {
			try {
				Field f = cls.getDeclaredField(name);
				return f;
			}
			catch (NoSuchFieldException e) {
				Class<?> scls = cls.getSuperclass();
				if (!Node.class.isAssignableFrom(scls)) {
					throw new NoSuchFieldException(name);
				}
				cls = (Class<? extends Node>) scls;
			}
		}
	}

}
