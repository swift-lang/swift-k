// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.workflow;

import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class JavaElement {
	private Class cls;

	public JavaElement(String className) {
		if (className == null || className.equals("null")) {
			throw new KarajanRuntimeException("class name is null");
		}
		try {
			this.cls = Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			throw new KarajanRuntimeException("Fatal: Class not found: " + className, e);
		}
	}
	
	public JavaElement(Class cls) {
		this.cls = cls;
	}

	public FlowElement newInstance() {
		try {
			Object instance = cls.newInstance();
			if (instance instanceof FlowElement) {
				return (FlowElement) instance;
			}
			else {
				throw new KarajanRuntimeException(
						"Fatal: Definition does not point to a FlowNode: " + cls.getName());
			}
		}
		catch (InstantiationException e) {
			throw new KarajanRuntimeException("Fatal: Cannot instantiate " + cls.getName(), e);
		}
		catch (IllegalAccessException e) {
			throw new KarajanRuntimeException("Fatal: Illegal access while instantiating "
					+ cls.getName(), e);
		}
	}

	public String toString() {
		return cls.getName();
	}

	public Class getElementClass() {
		return cls;
	}
}
