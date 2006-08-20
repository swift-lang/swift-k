// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Map;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class TrackingNamedArguments extends NamedArgumentsImpl {
	private final NamedArguments target;

	public TrackingNamedArguments(NamedArguments target) {
		this.target = target;
	}

	public synchronized void add(String name, Object value) {
		super.add(name, value);
		target.add(name, value);
	}

	public synchronized void addAll(Map args) {
		super.addAll(args);
		target.addAll(args);
	}

	public NamedArguments copy() {
		throw new KarajanRuntimeException("Should not use copy on TrackingNamedArguments");
	}

	public void merge(NamedArguments args) {
		super.merge(args);
		target.merge(args);
	}

	public void set(Map named) {
		throw new KarajanRuntimeException(
				"Cannot use set() on TrackingNamedArguments. This is an internal bug.");
	}

	public void set(NamedArguments other) {
		// ;)
		set(getAll());
	}
	
	public void close() {
		throw new UnsupportedOperationException();
	}
}