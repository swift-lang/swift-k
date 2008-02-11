// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Aug 6, 2003
 *  
 */
package org.globus.cog.karajan.stack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DefaultStackFrame implements StackFrame {
	private static final long serialVersionUID = 5576647919365350913L;
	
	private Map map = Collections.EMPTY_MAP;
	
	private final Regs regs = new Regs();

	public boolean isDefined(final String varName) {
		return map.containsKey(varName);
	}

	public Object getVar(final String name) {
		return map.get(name);
	}

	public void setVar(final String name, final Object value) {
		if (map.size() == 0) {
			map = new ListMap();
		}
		int osz = map.size();
		Object old = map.put(name, value);
		if (osz == 4 && old == null) {
			map = new HashMap(map);
			map.put(name, value);
		}
	}

	public void rename(final String oldName, final String newName) {
		map.put(newName, map.remove(oldName));
	}

	public void setIntVar(final String name, final int value) {
		setVar(name, new Integer(value));
	}

	public int getIntVar(final String name) throws VariableNotFoundException {
		return ((Integer) getVar(name)).intValue();
	}

	public void setBooleanVar(final String name, final boolean value) {
		setVar(name, Boolean.valueOf(value));
	}

	public boolean getBooleanVar(final String name) throws VariableNotFoundException {
		return ((Boolean) getVar(name)).booleanValue();
	}

	public void deleteVar(final String name) {
		map.remove(name);
	}

	public Collection names() {
		return map.keySet();
	}

	public boolean hasBarrier() {
		return regs.getBarrier();
	}

	public void setBarrier(final boolean barrier) {
		regs.setBarrier(barrier);
	}

	public Object getVarAndDelete(final String name) {
		return map.remove(name);
	}

	public synchronized int postIncrementAtomic(final String name) throws VariableNotFoundException {
		int val = getIntVar(name);
		setIntVar(name, val + 1);
		return val;
	}

	public synchronized int preDecrementAtomic(final String name) throws VariableNotFoundException {
		int val = getIntVar(name) - 1;
		setIntVar(name, val);
		return val;
	}

	public String toString() {
		Iterator i = map.entrySet().iterator();
		StringBuffer sb = new StringBuffer();
		sb.append(regs.toString());
		sb.append('\n');
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			sb.append('\t');
			sb.append(entry.getKey());
			sb.append(" = ");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

	public Regs getRegs() {
		return regs;
	}
}