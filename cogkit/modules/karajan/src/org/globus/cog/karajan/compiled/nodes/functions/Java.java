/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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