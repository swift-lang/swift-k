// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Mar 26, 2004
 *  
 */
package org.globus.cog.karajan.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.parser.ParseTree;
import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class TypeUtil {

	public static double toDouble(final Object obj) {
		try {
			if (obj instanceof String) {
				return Double.valueOf((String) obj).doubleValue();
			}
			else if (obj instanceof Number) {
				return ((Number) obj).doubleValue();
			}
			else {
				throw new KarajanRuntimeException("Could not convert value to number: " + obj);
			}
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to number: " + obj, e);
		}
	}

	public static Number toNumber(final Object obj) {
		try {
			if (obj instanceof String) {
				return Double.valueOf((String) obj);
			}
			else if (obj instanceof Number) {
				return (Number) obj;
			}
			else {
				throw new KarajanRuntimeException("Could not convert value to number: " + obj);
			}
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to number: " + obj, e);
		}
	}

	public static int toInt(final Object obj) {
		try {
			if (obj instanceof String) {
				return Integer.valueOf((String) obj).intValue();
			}
			else if (obj instanceof Number) {
				return ((Number) obj).intValue();
			}
			else {
				throw new KarajanRuntimeException("Could not convert value to integer: " + obj);
			}
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to number: " + obj, e);
		}
	}
	
	public static long toLong(final Object obj) {
		try {
			if (obj instanceof String) {
				return Long.valueOf((String) obj).longValue();
			}
			else if (obj instanceof Number) {
				return ((Number) obj).longValue();
			}
			else {
				throw new KarajanRuntimeException("Could not convert value to integer: " + obj);
			}
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to number: " + obj, e);
		}
	}


	public static boolean toBoolean(Object obj) {
		try {
			if (obj instanceof ParseTree) {
				obj = ((ParseTree) obj).execute(null);
			}
			if (obj instanceof String) {
				if (obj.equals("true") || obj.equals("yes")) {
					return true;
				}
				return false;
			}
			else if (obj instanceof Integer) {
				return ((Integer) obj).intValue() == 1;
			}
			else if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			else {
				throw new KarajanRuntimeException("Could not convert value to boolean: " + obj);
			}
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to boolean: " + obj, e);
		}
	}

	public static List toList(final Object obj) {
		try {
			if (obj instanceof List) {
				return (List) obj;
			}
			if (obj instanceof VariableArguments) {
				return ((VariableArguments) obj).getAll();
			}
			ArrayList l = new ArrayList();
			if (obj instanceof String) {
				StringTokenizer st = new StringTokenizer((String) obj, ",");
				while (st.hasMoreTokens()) {
					l.add(st.nextToken().trim());
				}
				return l;
			}
			if (obj == null) {
				throw new KarajanRuntimeException("Expected list but got " + null);
			}
			throw new KarajanRuntimeException("Expected list but got " + obj.getClass() + ": "
					+ obj);
		}
		catch (Exception e) {
			throw new KarajanRuntimeException("Could not convert value to list: " + obj, e);
		}
	}

	public static KarajanIterator toIterator(final Object obj) throws ExecutionException {
		try {
			if (obj instanceof KarajanIterator) {
				return (KarajanIterator) obj;
			}
			if (obj instanceof List) {
				return new ListKarajanIterator((List) obj);
			}
			if (obj instanceof VariableArguments) {
				return (KarajanIterator) ((VariableArguments) obj).iterator();
			}
			List l = new ArrayList();
			if (obj instanceof String) {
				StringTokenizer st = new StringTokenizer((String) obj, ",");
				while (st.hasMoreTokens()) {
					l.add(st.nextToken().trim());
				}
				return new ListKarajanIterator(l);
			}
			if (obj instanceof Map) {
				return new MapKeyKarajanIterator((Map) obj);
			}
			if (obj == null) {
				throw new ExecutionException("Expected iterator but got null");
			}
			throw new ExecutionException("Expected iterator but got " + obj.getClass() + ": " + obj);
		}
		catch (Exception e) {
			throw new ExecutionException("Could not convert value to iterator: " + obj, e);
		}
	}

	public static String toString(final Object obj) {
		if (obj instanceof String) {
			return (String) obj;
		}
		if (obj instanceof ParseTree) {
			return ((ParseTree) obj).getUnparsed();
		}
		if (obj instanceof Number) {
			final Number d = (Number) obj;
			if (Double.compare(d.doubleValue(), Math.round(d.doubleValue())) == 0) {
				return String.valueOf(d.longValue());
			}
			else {
				return d.toString();
			}
		}
		if (obj instanceof Identifier) {
			return ((Identifier) obj).getName();
		}
		if (obj instanceof Throwable) {
			if (obj instanceof ExecutionException) {
				ExecutionException ee = (ExecutionException) obj;
				StringBuffer sb = new StringBuffer();
				convertExecutionException(sb, ee);
				return sb.toString();
			}
			else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				Throwable t = (Throwable) obj;
				t.printStackTrace(ps);
				return baos.toString();
			}
		}
		if (obj instanceof String[]) {
			final StringBuffer sb = new StringBuffer();
			final String[] array = (String[]) obj;
			for (int i = 0; i < array.length; i++) {
				sb.append(array[i]);
				if (i > 0) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
		if (obj != null) {
			return obj.toString();
		}
		else {
			return null;
		}
	}
	
	private static void convertExecutionException(StringBuffer sb, ExecutionException ee) {
		sb.append(ee.getMessage());
		sb.append('\n');
		if (ee.getStack() != null) {
			sb.append(Trace.get(ee.getStack()));
		}
		else {
			sb.append("\t-- no stack --\n");
		}
		Throwable cause = ee.getCause();
		if (cause instanceof ExecutionException) {
			sb.append("Caused by: \n");
			convertExecutionException(sb, (ExecutionException) cause);
		}
		else if (cause != null) {
			sb.append("Caused by: ");
			sb.append(cause.getMessage());
		}
	}

	public static Integer toInteger(final Object obj) {
		return new Integer(toInt(obj));
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static String[] toStringArray(final Object obj) {
		if (obj == null) {
			return EMPTY_STRING_ARRAY;
		}
		if (obj instanceof String[]) {
			return (String[]) obj;
		}
		if (obj instanceof String) {
			final StringTokenizer st = new StringTokenizer((String) obj, " ,;");
			final String[] array = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++) {
				array[i] = st.nextToken();
			}
			return array;
		}
		throw new KarajanRuntimeException("Could not convert value to string array: " + obj);
	}

	public static final Identifier[] EMPTY_IDENTIFIER_ARRAY = new Identifier[0];

	public static Identifier[] toIdentifierArray(final Object obj) {
		if (obj == null) {
			return EMPTY_IDENTIFIER_ARRAY;
		}
		if (obj instanceof Identifier[]) {
			return (Identifier[]) obj;
		}
		if (obj instanceof String) {
			final StringTokenizer st = new StringTokenizer((String) obj, " ,;");
			final Identifier[] array = new Identifier[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++) {
				array[i] = new Identifier(st.nextToken());
			}
			return array;
		}
		throw new KarajanRuntimeException("Could not convert value to identifier array: " + obj);
	}

	public static Identifier toIdentifier(final Object obj) {
		if (obj instanceof Identifier) {
			return (Identifier) obj;
		}
		if (obj instanceof String) {
			return new Identifier((String) obj);
		}
		throw new KarajanRuntimeException("Invalid identifier: " + obj);
	}

	public static String[] toLowerStringArray(final String arg) {
		final String[] sa = toStringArray(arg);
		for (int i = 0; i < sa.length; i++) {
			sa[i] = sa[i].toLowerCase();
		}
		return sa;
	}

	public static String listToString(final List l) {
		if (l == null) {
			return "null";
		}
		final StringBuffer buf = new StringBuffer();
		final Object[] o = l.toArray();
		buf.append('[');
		if (o.length > 0) {
			buf.append(o[0].toString());
		}
		for (int i = 1; i < o.length; i++) {
			buf.append(", ");
			if (i < 100) {
				buf.append(o[i].toString());
			}
			else {
				buf.append("---");
				break;
			}
		}
		buf.append(']');
		return buf.toString();
	}
}