//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;


import k.rt.Stack;

public abstract class VarRef<T> extends Ref<T> {
	
	public abstract T getValue(Stack stack);
	
	public abstract T getValue();
	
	public abstract void setValue(Stack stack, T value);
	
	public abstract boolean isStatic();
	
	public static class Static<T> extends VarRef<T> {
		private final T value;
		
		public Static(T value) {
			this.value = value;
		}
		
		public T getValue(Stack stack) {
			return value;
		}

		@Override
		public void setValue(Stack stack, T value) {
			throw new UnsupportedOperationException();
		}
		
		public T getValue() {
			return value;
		}

		@Override
		public boolean isStatic() {
			return true;
		}
		
		public String toString() {
			return "StaticRef(" + value + ")";
		}
	}
	
	public static class DynamicLocal<T> extends VarRef<T> {
        public final int index;
        public final String name;
        
        public DynamicLocal(String name, int index) {
        	this.name = name;
            this.index = index;
        }        
        
        @SuppressWarnings("unchecked")
		public T getValue(Stack stack) {
            return (T) stack.top().get(index);
        }

		@Override
		public void setValue(Stack stack, T value) {
			stack.top().set(index, value);
			if (CompilerSettings.DEBUG) {
				stack.top().setName(index, name);
			}
		}
		
		public T getValue() {
            throw new UnsupportedOperationException();
        }

		@Override
		public boolean isStatic() {
			return false;
		}

		public String toString() {
			return "DynamicRef(0, " + index + ")";
		}
    }

	public static class Dynamic<T> extends VarRef<T> {
        protected final int frame, index;
        public final String name;
        
        public Dynamic(String name, int frame, int index) {
        	this.name = name;
            this.frame = frame;
            this.index = index;
        }        
        
        @SuppressWarnings("unchecked")
		public T getValue(Stack stack) {
            return (T) stack.getFrame(frame).get(index);
        }

		@Override
		public void setValue(Stack stack, T value) {
			stack.getFrame(frame).set(index, value);
			if (CompilerSettings.DEBUG) {
				stack.getFrame(frame).setName(index, name);
			}
		}
		
		public T getValue() {
            throw new UnsupportedOperationException();
        }

		@Override
		public boolean isStatic() {
			return false;
		}

		public String toString() {
			return "DynamicRef(" + frame + ", " + index + ")";
		}
    }
	
	public static class DynamicNotNull<T> extends Dynamic<T> {
		public DynamicNotNull(String name, int frame, int index) {
			super(name, frame, index);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getValue(Stack stack) {
			T o = (T) stack.getFrame(frame).get(index);
			if (o == null) {
				throw new RuntimeException("Variable not found: " + name);
			}
			return o;
		}		
	}
	
	public static class Buffer<T> extends DynamicLocal<T> {
		private final int destIndex;
		
		public Buffer(String name, int srcIndex, int destIndex) {
			super(name, srcIndex);
			this.destIndex = destIndex;
		}
		
		public void commit(Stack stack) {
			stack.top().set(destIndex, stack.top().get(index));
		}
	}
}
