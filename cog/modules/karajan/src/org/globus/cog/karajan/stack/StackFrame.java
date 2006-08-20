//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 29, 2005
 */
package org.globus.cog.karajan.stack;

import java.util.Collection;


public interface StackFrame {
	boolean isDefined(String varName);

	Object getVar(String name);

	void setVar(String name, Object value);

	void rename(String oldName, String newName);

	void setIntVar(String name, int value);

	int getIntVar(String name) throws VariableNotFoundException;
	
	int postIncrementAtomic(String name) throws VariableNotFoundException;

	void setBooleanVar(String name, boolean value);

	boolean getBooleanVar(String name) throws VariableNotFoundException;

	void deleteVar(String name);

	Collection names();

	boolean hasBarrier();

	void setBarrier(boolean barrier);

	Object getVarAndDelete(String name);

	int preDecrementAtomic(String name) throws VariableNotFoundException;
	
	Regs getRegs();
}