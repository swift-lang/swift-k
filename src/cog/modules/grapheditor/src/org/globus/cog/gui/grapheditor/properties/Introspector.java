// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.properties;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * An introspection helper. It includes support for user defined properties too,
 * when it comes to graph components.
 */
public class Introspector {
	private static Logger logger = Logger.getLogger(Introspector.class);

	private static Hashtable getterNames = new Hashtable();

	private static Hashtable classGetterTypes = new Hashtable();

	private static Hashtable declaredMethods = new Hashtable();

	private static Hashtable setterNames = new Hashtable();

	private static Hashtable classSetters = new Hashtable();

	private static Hashtable classGetters = new Hashtable();

	private static Object[] emptyObjectArray = new Object[0];

	/**
	 * If the object is not a graph component, introspective properties will be
	 * automatically generated.
	 * 
	 * @param o
	 * @return a list of properties for the object
	 */
	public static List getProperties(Object o) {
		if (o instanceof PropertyHolder) {
			return getPropertyHolderProperties((PropertyHolder) o);
		}
		List properties = new LinkedList();
		Class c = o.getClass();
		while (!c.equals(Object.class)) {
			Method[] methods = getDeclaredMethods(c);
			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getName();
				if (methodName.startsWith("get")) {
					if (_isWritable(o, getPropertyName(methodName))) {
						properties.add(new IntrospectiveProperty(o, getPropertyName(methodName),
								Property.RW));
					}
					else {
						properties.add(new IntrospectiveProperty(o, getPropertyName(methodName),
								Property.R));
					}
				}

			}
			c = c.getSuperclass();
		}
		return properties;
	}

	private synchronized static Method[] getDeclaredMethods(Class c) {
		Method[] methods = (Method[]) declaredMethods.get(c);
		if (methods == null) {
			methods = c.getDeclaredMethods();
			declaredMethods.put(c, methods);
		}
		return methods;
	}

	public static List getPropertyHolderProperties(PropertyHolder o) {
		LinkedList l = new LinkedList();
		Collection op = o.getProperties();
		Iterator i = op.iterator();
		while (i.hasNext()) {
			Property p = (Property) i.next();
			if (!p.isHidden()) {
				l.add(p);
			}
		}
		return l;
	}

	public static String getPropertyName(String getterName) {
		if (getterName.length() < 4) {
			return getterName;
		}
		return getterName.substring(3, 4).toLowerCase() + getterName.substring(4);
	}

	public static boolean _isWritable(Object o, String property) {
		Method[] methods = o.getClass().getDeclaredMethods();
		String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(setterName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param objects
	 * @return a list of <b>common </b> properties for all the specified objects
	 */
	public static List getCommonProperties(Collection objects) {
		List props = new LinkedList();
		Iterator i = objects.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (props.size() == 0) {
				props = getProperties(o);
			}
			else {
				List oProps = getProperties(o);
				if (props.size() != oProps.size()) {
					props = intersect(props, getProperties(o));
				}
			}
		}
		return props;
	}

	private static List intersect(List a, List b) {
		List r = new LinkedList();
		Iterator i = a.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (b.indexOf(o) != -1) {
				r.add(o);
			}
		}
		return r;
	}

	/**
	 * Gets a property value using introspection
	 * 
	 * @param o
	 * @param pname
	 * @return
	 */
	public static Object getProperty(Object o, String pname) {
		String getterName = getGetterName(pname);
		Class cls = o.getClass();
		Hashtable getters;
		synchronized (classGetters) {
			getters = (Hashtable) classGetters.get(cls);

			if (getters == null) {
				getters = new Hashtable();
				classGetters.put(o.getClass(), getters);
			}
		}
		Method getter;
		synchronized (getters) {
			getter = (Method) getters.get(getterName);
			if (getter == null) {
				Class[] argTypes = new Class[0];
				try {
					getter = cls.getMethod(getterName, argTypes);
					getters.put(getterName, getter);
				}
				catch (NoSuchMethodException e) {
					logger.error("Could find method " + getterName + " for class " + cls.getName(),
							e);
					return null;
				}
			}
		}
		try {
			Object retval = getter.invoke(o, emptyObjectArray);
			return retval;
		}
		catch (Exception e) {
			logger.error("Could invoke method " + getterName + " for class " + cls.getName(), e);
		}
		return null;
	}

	private static String getGetterName(String name) {
		if (getterNames.containsKey(name)) {
			return (String) getterNames.get(name);
		}
		else {
			String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			getterNames.put(name, getterName);
			return getterName;
		}
	}

	/**
	 * sets a property using introspection
	 * 
	 * @param o
	 * @param pname
	 * @param value
	 * @param valueClass
	 */
	public static void setProperty(Object o, String pname, Object value, Class valueClass) {
		String setterName;
		Class cls = o.getClass();
		if (setterNames.containsKey(pname)) {
			setterName = (String) setterNames.get(pname);
		}
		else {
			setterName = "set" + pname.substring(0, 1).toUpperCase() + pname.substring(1);
			setterNames.put(pname, setterName);
		}
		Hashtable setters;
		synchronized (classSetters) {
			setters = (Hashtable) classSetters.get(cls);
			if (setters == null) {
				setters = new Hashtable();
				classSetters.put(cls, setters);
			}
		}
		Method setter;
		synchronized (setters) {
			setter = (Method) setters.get(setterName);
			if (setter == null) {
				Class[] argTypes = new Class[1];
				argTypes[0] = valueClass;
				try {
					setter = o.getClass().getMethod(setterName, argTypes);
					setters.put(setterName, setter);
				}
				catch (NoSuchMethodException e) {
					logger.error("No such setter (" + setterName + ") for class " + cls.getName(),
							e);
					return;
				}
			}
		}
		Object[] args = new Object[1];
		args[0] = value;
		try {
			setter.invoke(o, args);
		}
		catch (Exception e) {
			logger.error("Could not invoke " + setterName + " for class " + cls.getName(), e);
			e.printStackTrace();
		}
	}

	public static Class getPropertyClass(Object o, String name) {
		return getPropertyClass(o.getClass(), name);
	}

	/**
	 * determines the class of a property using instrospection. It looks at the
	 * parameter the setter takes for that property
	 * 
	 * @param o
	 * @param name
	 * @return
	 */
	public static Class getPropertyClass(Class c, String name) {
		String getterName = getGetterName(name);
		Hashtable getters;
		synchronized (classGetterTypes) {
			getters = (Hashtable) classGetterTypes.get(c);
			if (getters == null) {
				getters = new Hashtable();
				classGetterTypes.put(c, getters);
			}
		}
		Class cls;
		synchronized (getters) {
			cls = (Class) getters.get(getterName);
			if (cls != null) {
				return cls;
			}
			else {
				cls = c;
				while (cls != null) {
					Method[] methods = getDeclaredMethods(cls);
					for (int i = 0; i < methods.length; i++) {
						if (methods[i].getName().equals(getterName)) {
							Class pc = methods[i].getReturnType();
							getters.put(getterName, pc);
							return pc;
						}
					}
					cls = cls.getSuperclass();
				}
			}
		}
		throw new RuntimeException(c.getName() + " does not have a method named " + getterName);
	}
}