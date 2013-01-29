// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 15, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;

public class Java {
	
	public static class ClassOf extends AbstractSingleValuedFunction {
		private ArgRef<Object> object;

		@Override
		protected Param[] getParams() {
			return params("object");
		}

		@Override
		public Object function(Stack stack) {
			Object o = this.object.getValue(stack);
			if (o == null) {
				return "null";
			}
			else {
				return o.getClass().getName();
			}
		}
		
	}
	
	public static class GetField extends AbstractSingleValuedFunction {
		private ArgRef<String> field;
		private ArgRef<Object> object;
		private ArgRef<String> classname;

		@Override
		protected Param[] getParams() {
			return params("field", optional("classname", null), optional("object", null));
		}

		@Override
		public Object function(Stack stack) {
			Object o = this.object.getValue(stack);
			Class<?> cls;
			if (o != null) {
				cls = o.getClass();
			}
			else {
				String className = this.classname.getValue(stack);
				if (className == null) {
					throw new ExecutionException(this, "Missing both class name and object parameters");
				}
				try {
					cls = Class.forName(className);
				}
				catch (ClassNotFoundException e) {
					throw new ExecutionException(this, "Invalid class name: " + className);
				}
			}
			try {
				return cls.getField(this.field.getValue(stack)).get(o);
			}
			catch (Exception e) {
				throw new ExecutionException("Could not get the specified field", e);
			}
		}
	}

	public static class SystemProperty extends AbstractSingleValuedFunction {
		private ArgRef<String> name;

		@Override
		protected Param[] getParams() {
			return params("name");
		}

		@Override
		public Object function(Stack stack) {
			return System.getProperty(name.getValue(stack));
		}
	}
}