//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 13, 2006
 */
package org.globus.cog.karajan.scheduler;

public class TaskTransformerFactory {
	public static TaskTransformer newFromClass(String className) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class cls = TaskTransformerFactory.class.getClassLoader().loadClass(className);
		return (TaskTransformer) cls.newInstance();
	}
}
