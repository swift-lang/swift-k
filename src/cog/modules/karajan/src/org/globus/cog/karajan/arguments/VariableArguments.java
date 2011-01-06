//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Iterator;
import java.util.List;

public interface VariableArguments {
	void merge(VariableArguments args);

	void append(Object value);

	void appendAll(List args);

	List getAll();

	void set(List vargs);

	Object get(int index);

	VariableArguments copy();

	int size();

	Iterator iterator();

	Object[] toArray();

	void set(VariableArguments other);

	Object removeFirst();
	
	void addListener(VariableArgumentsListener l);
	
	void removeListener(VariableArgumentsListener l);

	boolean isEmpty();
	
	VariableArguments butFirst();
	
	boolean isCommutative();
}