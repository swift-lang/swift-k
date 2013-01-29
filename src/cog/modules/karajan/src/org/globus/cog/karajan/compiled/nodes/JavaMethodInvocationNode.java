// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractMultiValuedFunction;
import org.globus.cog.karajan.util.TypeUtil;

public class JavaMethodInvocationNode extends AbstractMultiValuedFunction {
	private ArgRef<String> method;
	private ArgRef<String> classname;
	private ArgRef<Object> object;
	private ArgRef<Object> types;
	private ChannelRef<Object> c_vargs;

	protected static final Map<String, Class<?>> TYPES = new HashMap<String, Class<?>>();

	static {
		TYPES.put("boolean", boolean.class);
		TYPES.put("int", int.class);
		TYPES.put("float", float.class);
		TYPES.put("double", double.class);
		TYPES.put("char", char.class);
		TYPES.put("long", long.class);
		TYPES.put("String", String.class);
	}

	
	@Override
	protected Signature getSignature() {
		return new Signature(params("method", optional("classname", null), optional("object", null), optional("types", null), "..."), 
				returns(channel("...", DYNAMIC)));
	}

	public Object function(Stack stack) {
		String method = this.method.getValue(stack);
		String classname = this.classname.getValue(stack);
		Object o = this.object.getValue(stack);
		Object otypes = this.types.getValue(stack);
		if (classname != null && o != null) {
			throw new ExecutionException(this, "Only one of 'classname' and 'object' can be specified");
		}
		Object[] args = c_vargs.get(stack).toArray();
		Class<?>[] argTypes = new Class[args.length];
		if (otypes != null) {
			List<?> types = TypeUtil.toList(otypes);
			if (types.size() != args.length) {
				throw new ExecutionException(this, 
						"The number of items in the types attribute does not match the number of arguments");
			}
			Iterator<?> i = types.iterator();
			for (int j = 0; j < argTypes.length; j++) {
				String type = (String) i.next();
				argTypes[j] = getClass(this, type);
				if (TYPES.containsKey(type)) {
					args[j] = convert(argTypes[j], args[j], argTypes[j].isArray());
				}
				else {
					if (args[j] != null) {
						if (!argTypes[j].isAssignableFrom(args[j].getClass())) {
							args[j] = convert(argTypes[j], args[j], argTypes[j].isArray());
						}
					}
				}
			}
		}
		else {
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					argTypes[i] = null;
				}
				else {
					argTypes[i] = args[i].getClass();
				}
			}
		}
		try {
			Class<?> cls;
			if (o == null) {
				cls = Class.forName(classname);
			}
			else {
				cls = o.getClass();
			}
			return cls.getMethod(method, argTypes).invoke(o, args);
		}
		catch (InvocationTargetException e) {
			throw new ExecutionException(this, e.getTargetException().getClass().getName()
					+ " caught while invoking Java method: " + e.getTargetException().getMessage(),
					e.getTargetException());
		}
		catch (Exception e) {
			if (o == null) {
				throw new ExecutionException(this, "Could not invoke static java method " + method
						+ " with arguments " + prettyPrintArray(args) + " on class " + classname
						+ " because of: " + e.getClass().getName() + ":" + e.getMessage(), e);
			}
			else {
				throw new ExecutionException(this, "Could not invoke java method " + method
						+ " with arguments " + prettyPrintArray(args) + " on an instance of class "
						+ o.getClass() + " because of: " + e.getClass().getName() + ":"
						+ e.getMessage(), e);
			}
		}
	}

	public static Class<?> getClass(Node fn, String type) {
		boolean array = false;
		if (type.endsWith("[]")) {
			array = true;
			type = type.substring(0, type.length() - 2);
		}
		if (TYPES.containsKey(type)) {
			if (array) {
				return Array.newInstance(TYPES.get(type), 0).getClass();
			}
			else {
				return TYPES.get(type);
			}
		}
		else {
			try {
				if (array) {
					return Array.newInstance(Class.forName(type), 0).getClass();
				}
				else {
					return Class.forName(type);
				}
			}
			catch (ClassNotFoundException e1) {
				throw new ExecutionException(fn, "Invalid type: " + type);
			}
		}
	}

	protected static Object convert(Class<?> type, Object src, boolean array) {
		if (array) {
			if (type.getComponentType().equals(char.class) && src instanceof String) {
				return ((String) src).toCharArray();
			}
			else {
				List<?> l = TypeUtil.toList(src);
				Object ar;
				if (l != null) {
					ar = Array.newInstance(type.getComponentType(), l.size());
				}
				else {
					ar = Array.newInstance(type.getComponentType(), 0);
				}
				for (int i = 0; i < l.size(); i++) {
					Array.set(ar, i, convert(type.getComponentType(), l.get(i)));
				}
				return ar;
			}
		}
		else {
			return convert(type, src);
		}
	}

	protected static Object convert(Class<?> type, Object src) throws ExecutionException {
		if (type.isAssignableFrom(src.getClass())) {
			return src;
		}
		if (src.getClass().equals(String.class)) {
			String val = (String) src;
			if (type.equals(Boolean.TYPE)) {
				return Boolean.valueOf(val);
			}
			if (type.equals(Integer.TYPE)) {
				return new Integer(val);
			}
			if (type.equals(Float.TYPE)) {
				return new Float(val);
			}
			if (type.equals(Double.TYPE)) {
				return new Double(val);
			}
			if (type.equals(Character.TYPE)) {
				return new Character(val.charAt(0));
			}
		}
		if (src.getClass().equals(type)) {
			return src;
		}
		if (src.getClass().equals(Boolean.class) && type.equals(Boolean.TYPE)) {
			return src;
		}
		if (src instanceof Number) {
			Number num = (Number) src;
			if (type.equals(int.class)) {
				return new Integer(num.intValue());
			}
			if (type.equals(long.class)) {
				return new Long(num.longValue());
			}
			if (type.equals(float.class)) {
				return new Float(num.floatValue());
			}
			if (type.equals(double.class)) {
				return new Double(num.doubleValue());
			}
		}
		if (type.equals(java.util.Date.class)) {
			if (src instanceof String) {
				DateFormat df = DateFormat.getInstance();
				try {
					return df.parse((String) src);
				}
				catch (ParseException e) {
					throw new ExecutionException("Could not parse date " + src + " ("
							+ e.getMessage() + "). A valid one is " + df.format(new Date()), e);
				}
			}
		}
		if (type.equals(java.io.File.class) && (src instanceof String)) {
			return new java.io.File((String) src);
		}
		if (type.equals(Double.TYPE) && (src instanceof Date)) {
			return new Double(((Date) src).getTime());
		}
		throw new ExecutionException("Could not convert a " + src.getClass() + " to " + type);
	}

	public static String prettyPrintArray(Object[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			if (array[i] instanceof String) {
				sb.append('"');
				sb.append(array[i]);
				sb.append('"');
			}
			else {
				sb.append(array[i]);
			}
		}
		sb.append(']');
		return sb.toString();
	}
}