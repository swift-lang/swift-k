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
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.util.HashMap;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Param;

public class Map {
	
	public static class Entry implements java.util.Map.Entry<Object, Object> {
		public final Object key;
		public Object value;
				
		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		public Object getKey() {
			return key;
		}
		
		public Object getValue() {
			return value;
		}
		
		public Object setValue(Object value) {
			Object old = this.value;
			this.value = value;
			return old;
		}
		
		public String toString() {
			return key + " -> " + value;
		}
	}
	
	public static class Cons extends AbstractSingleValuedFunction {
		private ChannelRef<Object> c_vargs;

		@Override
		protected Param[] getParams() {
			return params("...");
		}

		@Override
		public Object function(Stack stack) {
			java.util.Map<Object, Object> map = new HashMap<Object, Object>();
			java.util.List<Object> l = c_vargs.get(stack).getAll();
			for (Object o : l) {
				if (o instanceof java.util.Map.Entry) {
					java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) o;
					map.put(entry.getKey(), entry.getValue());
				}
				else if (o instanceof java.util.Map) {
					map.putAll((java.util.Map<?, ?>) o);
				}
				else {
					throw new ExecutionException("Invalid argument (must be map:entry or map): "
						+ o + "(class " + o.getClass() + ")");
				}
			}
			return map;
		}
	}
	
	public static class Put extends AbstractSingleValuedFunction {
		private ArgRef<java.util.Map<Object, Object>> map;
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Param[] getParams() {
			return params("map", "...");
		}

		@Override
		public Object function(Stack stack) {
			java.util.Map<Object, Object> map = this.map.getValue(stack);
			java.util.List<Object> l = c_vargs.get(stack).getAll();
			for (Object o : l) {
				if (l instanceof java.util.Map.Entry) {
					java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) o;
					map.put(entry.getKey(), entry.getValue());
				}
				else if (o instanceof java.util.Map) {
					map.putAll((java.util.Map<?, ?>) o);
				}
				else {
					throw new ExecutionException("Invalid argument (must be map:entry or map): "
						+ o + "(class " + o.getClass() + ")");
				}
			}
			return map;
		}
	}
	
	public static class EntryCons extends AbstractSingleValuedFunction {
		private ArgRef<Object> key;
		private ArgRef<Object> value;
		
		@Override
		protected Param[] getParams() {
			return params("key", "value");
		}

		@Override
		public Object function(Stack stack) {
			return new Entry(key.getValue(stack), value.getValue(stack));
		}
	}
	
	public static class Get extends AbstractSingleValuedFunction {
		private ArgRef<Object> key;
		private ArgRef<java.util.Map<Object, Object>> map;
		
		@Override
		protected Param[] getParams() {
			return params("key", "map");
		}

		@Override
		public Object function(Stack stack) { 
			Object key = this.key.getValue(stack);
			java.util.Map<Object, Object> map = this.map.getValue(stack);
			Object value = map.get(key);
			if (value == null && !map.containsKey(key)) {
				throw new ExecutionException(this, "No such key: " + key);
			}
			return value;
		}
	}
	
	public static class Delete extends AbstractSingleValuedFunction {
		private ArgRef<Object> key;
		private ArgRef<java.util.Map<Object, Object>> map;
		
		
		@Override
		protected Param[] getParams() {
			return params("key", "map");
		}

		@Override
		public Object function(Stack stack) { 
			Object key = this.key.getValue(stack);
			java.util.Map<Object, Object> map = this.map.getValue(stack);
			if (!map.containsKey(key)) {
				throw new ExecutionException(this, "No such key: " + key);
			}
			return map.remove(key);
		}
	}
	
	public static class Size extends AbstractSingleValuedFunction {
		private ArgRef<java.util.Map<Object, Object>> map;
		
		@Override
		protected Param[] getParams() {
			return params("map");
		}

		@Override
		public Object function(Stack stack) { 
			java.util.Map<Object, Object> map = this.map.getValue(stack);
			return map.size();
		}
	}

	public static class Contains extends AbstractSingleValuedFunction {
		private ArgRef<Object> key;
		private ArgRef<java.util.Map<Object, Object>> map;
		
		@Override
		protected Param[] getParams() {
			return params("key", "map");
		}

		@Override
		public Object function(Stack stack) { 
			Object key = this.key.getValue(stack);
			java.util.Map<Object, Object> map = this.map.getValue(stack);
			return map.containsKey(key);
		}
	}
}