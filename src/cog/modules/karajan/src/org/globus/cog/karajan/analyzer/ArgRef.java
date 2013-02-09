//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import k.rt.Frame;
import k.rt.FutureObject;
import k.rt.Stack;

import org.globus.cog.karajan.compiled.nodes.Node;


public abstract class ArgRef<T> {
	
	public abstract T getValue(Stack stack);
	
	public abstract T getValue();
	
	public abstract boolean isStatic();
	
	public static class Static<T> extends ArgRef<T> {
		private final T value;
		
		public Static(T value) {
			this.value = value;
		}

		@Override
		public T getValue(Stack stack) {
			return value;
		}

		@Override
		public T getValue() {
			return value;
		}
		
		@Override
		public boolean isStatic() {
			return true;
		}

		@Override
		public String toString() {
			return "AS[" + value + "]";
		}
	}
	
	public static class Dynamic<T> extends ArgRef<T> {
        protected final int index;
        
        public Dynamic(int index) {
            this.index = index;
        }

        @SuppressWarnings("unchecked")
		@Override
        public T getValue(Stack stack) {
        	Object o = stack.top().get(index);
        	if (o instanceof FutureObject) {
        		return (T) ((FutureObject) o).getValue();
        	}
        	else {
        		return (T) o;
        	}
        }

		@Override
		public T getValue() {
			return null;
		}

		@Override
		public boolean isStatic() {
			return false;
		}
		
		@Override
		public String toString() {
			return "AD[" + index + "]";
		}
    }
	
	public static class RuntimeOptional<T> extends Dynamic<T> {
		private Node fn;
		private final String name;
		private T value;
		
		public RuntimeOptional(int index, String name, Node fn) {
			super(index);
			this.name = name;
			this.fn = fn;
		}
		
		public Node getNode() {
			return fn;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void set(Frame dst) {
			dst.set(index, value);
		}
		
		public void set(Frame dst, int offset) {
            dst.set(index + offset, value);
        }
		
		public String toString() {
		    return name + " = " + value;
		}
	}
	
	public static class DynamicOptional<T> extends ArgRef<T> {
        private final int index;
        private final T value;
        
        public DynamicOptional(int index, T value) {
            this.index = index;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
		@Override
        public T getValue(Stack stack) {
            T v = (T) stack.top().get(index);
            if (v == null) {
            	return value;
            }
            else {
            	return v;
            }
        }

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public boolean isStatic() {
			return false;
		}
		
		@Override
		public String toString() {
			return "ADO[" + index + ", " + value + "]";
		}
    }
	
	public static class DynamicOptionalNull<T> extends ArgRef<T> {
        private final int index;
        private final String name;
        
        public DynamicOptionalNull(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @SuppressWarnings("unchecked")
		@Override
        public T getValue(Stack stack) {
            T v = (T) stack.top().get(index);
            if (v == null) {
            	throw new IllegalArgumentException("Missing argument value for '" + name + "'");
            }
            else {
            	return v;
            }
        }

		@Override
		public T getValue() {
			return null;
		}

		@Override
		public boolean isStatic() {
			return false;
		}
		
		@Override
		public String toString() {
			return "ADON[" + index + "]";
		}
    }
}
