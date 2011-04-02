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
import java.util.Map;

public interface NamedArguments {
	void merge(NamedArguments args);

	void addAll(Map args);

	void add(String name, Object value);
	
	void add(Arg arg, Object value);

	Iterator getNames();

	Object getArgument(String name);

	boolean hasArgument(String name);

	Map getAll();

	void set(Map named);

	void set(NamedArguments other);

	NamedArguments copy();

	int size();
	
	void addListener(String name, NamedArgumentsListener l);
}