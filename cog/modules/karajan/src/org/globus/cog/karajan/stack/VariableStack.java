//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 29, 2005
 */
package org.globus.cog.karajan.stack;

import java.util.List;

import org.globus.cog.karajan.workflow.ExecutionContext;


public interface VariableStack {
	void enter();

	void leave();

	int frameCount();

	boolean isDefined(String varName);

	Object getVar(String name) throws VariableNotFoundException;
	
	Object getDeepVar(String name) throws VariableNotFoundException;
	
	Object getShallowVar(String name) throws VariableNotFoundException;
	
	Object getVarFromFrame(String name, int skipCount) throws VariableNotFoundException;

	List getAllVars(String name);

	String getVarAsString(String varName) throws VariableNotFoundException;

	StackFrame currentFrame();

	StackFrame parentFrame();

	StackFrame firstFrame();
	
	StackFrame getFrame(int frame);

	void setVar(String name, Object value);

	void exportVar(String name);

	VariableStack copy();

	String toString();

	void dumpAll();

	void setVar(String name, int value);

	int getIntVar(String name) throws VariableNotFoundException;

	void setVar(String name, boolean value);

	boolean getBooleanVar(String name) throws VariableNotFoundException;

	void setBarrier();

	void setGlobal(String name, Object value);

	Object getGlobal(String name);
	
	VariableStack newInstance();
	
	Regs getRegs();
	
	ExecutionContext getExecutionContext();
}