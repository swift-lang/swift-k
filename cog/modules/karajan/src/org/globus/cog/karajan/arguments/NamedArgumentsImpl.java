// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 5, 2004
 */
package org.globus.cog.karajan.arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class NamedArgumentsImpl implements NamedArguments {
	private Map named;
	private Map listeners;
	private Set valid;
	private transient FlowElement owner;

	public NamedArgumentsImpl() {
	}

	public NamedArgumentsImpl(Set valid, FlowElement owner) {
		this.valid = valid;
		this.owner = owner;
	}

	public NamedArgumentsImpl(Map map) {
		named = new HashMap(map);
	}

	public synchronized void merge(NamedArguments args) {
		if (listeners == null) {
			if (named == null) {
				named = new HashMap();
			}
			named.putAll(args.getAll());
		}
		else {
			addAll(args.getAll());
		}
	}

	public synchronized void addAll(Map args) {
		if (args == null) {
			return;
		}
		if (named == null) {
			named = new HashMap();
		}
		Iterator i = args.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			add((String) entry.getKey(), entry.getValue());
		}
	}

	public synchronized void add(String name, Object value) {
		if (valid != null && !valid.contains(name)) {
			if (owner != null) {
				throw new UnsupportedArgumentException(owner + " does not support a '" + name
						+ "' argument.");
			}
			else {
				throw new UnsupportedArgumentException("Unsupported argument: " + name);
			}
		}
		if (named == null) {
			named = new HashMap(4);
		}
		if (name == null) {
			throw new KarajanRuntimeException("Null name");
		}
		named.put(name, value);
		changed(name);
	}
	
	public void add(Arg arg, Object value) {
	    add(arg.getName(), value);
	}

	public Iterator getNames() {
		if (named == null) {
			return new EmptyIterator();
		}
		return named.keySet().iterator();
	}

	public Object getArgument(String name) {
		if (named != null) {
			return named.get(name);
		}
		return null;
	}

	public boolean hasArgument(String name) {
		if (named == null) {
			return false;
		}
		return named.containsKey(name);
	}

	public synchronized Map getAll() {
		if (named == null) {
			return Collections.EMPTY_MAP;
		}
		return named;
	}

	public void set(Map named) {
		this.named = named;
	}

	public void set(NamedArguments other) {
		this.named = other.getAll();
	}

	public boolean equals(Object obj) {
		if (obj instanceof NamedArguments) {
			NamedArgumentsImpl other = (NamedArgumentsImpl) obj;
			if ((named == null) && (other.named != null)) {
				return false;
			}
			if ((named != null) && !named.equals(other.named)) {
				return false;
			}
			return true;
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		if (named != null) {
			return named.hashCode();
		}
		return getClass().hashCode();
	}

	private static class EmptyIterator implements Iterator {

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}
	}

	public String toString() {
		return "Named: " + getAll();
	}

	public NamedArguments copy() {
		NamedArguments n = new NamedArgumentsImpl();
		if (this.named != null) {
			n.set(new HashMap(this.named));
		}
		return n;
	}

	public int size() {
		int sz = 0;
		if (named != null) {
			sz += named.size();
		}
		return sz;
	}

	private void changed(String name) {
		if (listeners == null) {
			return;
		}
		Set ls = (Set) listeners.get(name);
		if (ls == null) {
			return;
		}
		Iterator i = ls.iterator();
		while (i.hasNext()) {
			((NamedArgumentsListener) i.next()).namedArgumentAdded(name, this);
		}
	}

	public synchronized void addListener(String name, NamedArgumentsListener l) {
		if (listeners == null) {
			listeners = new Hashtable();
		}
		Set ls = (Set) listeners.get(name);
		if (ls == null) {
			ls = new HashSet();
			listeners.put(name, ls);
		}
		ls.add(l);
	}
}