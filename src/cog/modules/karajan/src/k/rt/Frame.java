//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 6, 2012
 */
package k.rt;

import org.globus.cog.karajan.analyzer.CompilerSettings;


public class Frame {
	private static final Object[] EMPTY_FRAME = new Object[0];
	
	private final Object owner;
	private final Object[] a;
	private String[] names;
	protected final Frame prev;
	
	public Frame(Object owner, int count, Frame prev) {
		if (count > 0) {
			a = new Object[count];
			if (CompilerSettings.DEBUG) {
				names = new String[count];
			}
		}
		else {
			a = EMPTY_FRAME;
		}
		this.prev = prev;
		this.owner = owner;
	}

	public void set(int i, Object value) {
		a[i] = value;
	}
	
	public Object get(int i) {
		return a[i];
	}
	
	public int size() {
		return a.length;
	}

	public Object[] getAll() {
		return a;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(owner);
		sb.append(":");
		for (int i = 0; i < a.length; i++) {
			sb.append("\n\t");
			sb.append(i);
			sb.append(" - ");
			if (CompilerSettings.DEBUG) {
				if (names[i] != null) {
					sb.append(names[i]);
					sb.append(" = ");
				}
			}
			String s = str(a[i]);
			if (s.length() > 32) {
				sb.append(s.subSequence(0, 24));
				sb.append("... ");
				sb.append(s.subSequence(s.length() - 8, s.length()));
			}
			else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	private String str(Object object) {
		if (object instanceof String) {
			return "\"" + object + '"';
		}
		else if (object == null) {
			return "";
		}
		else {
			return String.valueOf(object);
		}
	}

	public void setName(int index, String name) {
		names[index] = name;
	}
	
	public Frame prev() {
		return prev;
	}
}
