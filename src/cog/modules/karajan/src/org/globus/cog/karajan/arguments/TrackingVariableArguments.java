// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.List;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class TrackingVariableArguments extends VariableArgumentsImpl {
	private final VariableArguments target;

	public TrackingVariableArguments(VariableArguments target) {
		this.target = target;
	}

	public synchronized void append(Object value) {
		super.append(value);
		target.append(value);
	}

	public synchronized void appendAll(List args) {
		super.appendAll(args);
		target.appendAll(args);
	}

	public VariableArguments copy() {
		throw new KarajanRuntimeException("You should not use copy() on TrackingVariableArguments");
	}

	public void merge(VariableArguments args) { 
		super.merge(args);
		target.merge(args);
	}

	public Object removeFirst() {
		throw new KarajanRuntimeException("removeFirst() is not supported on TrackingVariableArguments");
	}

	public void set(List vargs) {
		throw new KarajanRuntimeException("set() is not supported on TrackingVariableArguments");
	}

	public void set(VariableArguments other) {
		set(other.getAll());
	}
	
	public void close() {
		throw new KarajanRuntimeException("close() should not be called on tracking vargs");
	}
}