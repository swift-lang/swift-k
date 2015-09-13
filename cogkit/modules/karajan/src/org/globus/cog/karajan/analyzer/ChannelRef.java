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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.ArrayList;
import java.util.List;

import k.rt.Channel;
import k.rt.EmptyChannel;
import k.rt.FixedArgChannel;
import k.rt.FixedArgChannelDebug;
import k.rt.FixedFutureArgChannel;
import k.rt.FutureMemoryChannel;
import k.rt.MemoryChannel;
import k.rt.OrderedParallelChannel;
import k.rt.ReadOnlyChannel;
import k.rt.SingleValueChannel;
import k.rt.Sink;
import k.rt.Stack;
import k.rt.VarArgChannel;
import k.rt.VarFutureArgChannel;

import org.globus.cog.karajan.compiled.nodes.Node;


public abstract class ChannelRef<T> {
	
	public abstract Channel<T> get(Stack stack);
	
	public abstract void create(Stack stack);
	
	public abstract void set(Stack stack, Channel<T> value);
	
	public abstract int getFrame();
	
	public void close(Stack stack) {
		get(stack).close();
	}
	
	public void append(Stack stack, T value) {
		get(stack).add(value);
	}
	
	public void check(Stack stack) {
		// implementations that require restrictions
		// on the structure of the channel (like the number of items)
		// should override this
	}
	
	public void clean(Stack stack) {
    }
	
	public StaticChannel getValue() {
		throw new UnsupportedOperationException();
	}
	
	public static class Return<T> extends ChannelRef<T> {
		private final int frame, index;
		private String name;
		
		public Return(int frame, int index) {
			this.frame = frame;
			this.index = index;
		}
		
		public Return(Var.Channel cv) {
			this.frame = cv.getFrame();
			this.index = cv.getIndex();
			this.name = cv.name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Channel<T> get(Stack stack) {
			return (Channel<T>) stack.getFrame(frame).get(index);
		}

		@Override
		public void create(Stack stack) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
			stack.getFrame(frame).set(index, value);
		}

		public int getFrame() {
			return frame;
		}

		public int getIndex() {
			return index;
		}
		
		public String toString() {
			return "&CR(" + (name == null ? "" : name + ", ") + frame + ", " + index + ")";
		}
	}
	
	public static class ReturnSingleValued<T> extends ChannelRef<T> {
		private final int frame, index;
		
		public ReturnSingleValued(int frame, int index) {
			this.frame = frame;
			this.index = index;
		}
		
		public ReturnSingleValued(Var.Channel cv) {
			this.frame = cv.getFrame();
			this.index = cv.getIndex();
		}

		@Override
		public Channel<T> get(final Stack stack) {
			return new Sink<T>() {
				private boolean set;
				@Override
				public boolean add(T value) {
					if (set) {
						throw new IndexOutOfBoundsException();
					}
					stack.getFrame(frame).set(index, value);
					set = true;
					return true;
				}
			};
		}

		@Override
		public void append(Stack stack, T value) {
			stack.getFrame(frame).set(index, value);
		}

		@Override
		public void create(Stack stack) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
			throw new UnsupportedOperationException();
		}

		public int getFrame() {
			return frame;
		}

		public int getIndex() {
			return index;
		}
		
		public String toString() {
			return "&CR(" + frame + ", " + index + ")";
		}
	}

	
	public static class Dynamic<T> extends ChannelRef<T> {
		protected final int index;
		protected final String name;
		
		public Dynamic(String name, int index) {
			this.name = name;
			this.index = index;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Channel<T> get(Stack stack) {
			return (Channel<T>) stack.top().get(index);
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, new MemoryChannel<T>());
		}
		
		public void set(Stack stack, Channel<T> value) {
			stack.top().set(index, value);
			if (CompilerSettings.DEBUG) {
				stack.top().setName(index, name);
			}
		}
		
		public String toString() {
			return "&CD(" + index + ")";
		}

		@Override
		public int getFrame() {
			return 0;
		}
	}
	
	public static class DynamicSingleValued<T> extends Dynamic<T> {
		public DynamicSingleValued(String name, int index) {
            super(name, index);
        }

		@Override
		public void create(Stack stack) {
			stack.top().set(index, new SingleValueChannel<T>());
		}
	}
	
	public static class DynamicFuture<T> extends Dynamic<T> {

		public DynamicFuture(String name, int index) {
			super(name, index);
		}
		
		@Override
		public void create(Stack stack) {
			stack.top().set(index, new FutureMemoryChannel<T>());
		}
		
		public String toString() {
			return "&FCD(" + index + ")";
		}
	}
	
	public static class SingleValued<T> extends ChannelRef<T> {
		protected final String name;
		private final int index;
		
		public SingleValued(String name, int index) {
			this.name = name;
			this.index = index;
		}

		@Override
		public Channel<T> get(Stack stack) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, null);
			if (CompilerSettings.DEBUG) {
				stack.top().setName(index, name);
			}
		}
		
		@Override
		public void append(Stack stack, T value) {
			Object old = stack.top().get(index);
			if (old != null) {
				throw new IllegalArgumentException(name);
			}
			stack.top().set(index, value);
		}

		public void set(Stack stack, Channel<T> value) {
			throw new UnsupportedOperationException();
		}
		
		@SuppressWarnings("unchecked")
		public T getValue(Stack stack) {
			return (T) stack.top().get(index);
		}
		
		public String toString() {
			return "&CSV()";
		}

		@Override
		public int getFrame() {
			return 0;
		}
	}

		
	public static class Static<T> extends ChannelRef<T> {
		protected final Channel<T> value;
		
		public Static(List<T> value) {
			this.value = new ReadOnlyChannel<T>(value);
		}
		
		public Static(Channel<T> value) {
			this.value = value;
		}

		@Override
		public Channel<T> get(Stack stack) {
			return value;
		}

		@Override
		public void create(Stack stack) {
			// do nothing
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
			// do nothing
		}
		
		public String toString() {
			return "&CS(" + value + ")";
		}

		@Override
		public int getFrame() {
			return 0;
		}
	}
	
	public static class DynamicPreset<T> extends Static<T> {
		private final int index;
		
		public DynamicPreset(int index, Channel<T> value) {
			super(value);
			this.index = index;
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, value);
		}
		
		public String toString() {
			return "&CDP(" + value + ")";
		}
	}
		
	public static class Mixed<T> extends Dynamic<T> {
		protected final List<T> value;
		
		public Mixed(int index, List<T> value) {
			super("...", index);
			this.value = value;
		}

		@Override
		public void create(Stack stack) {
			MemoryChannel<T> c = new MemoryChannel<T>();
			c.addAll(value);
			stack.top().set(index, c);
		}
		
		public String toString() {
			return "&CM(" + index + ")";
		}
	}
	
	public static class MixedFuture<T> extends Mixed<T> {

		public MixedFuture(int index, List<T> value) {
			super(index, value);
		}
		
		@Override
		public void create(Stack stack) {
			FutureMemoryChannel<T> c = new FutureMemoryChannel<T>();
			c.addAll(value);
			stack.top().set(index, c);
		}
		
		public String toString() {
			return "&FCM(" + index + ")";
		}
	}
	
	private static class Empty<T> extends ChannelRef<T> {
		private final Channel<T> value = new EmptyChannel<T>();
		
		@Override
		public Channel<T> get(Stack stack) {
			return value;
		}

		@Override
		public void create(Stack stack) {
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
		}
		
		public String toString() {
			return "&CE()";
		}

		@Override
		public int getFrame() {
			return 0;
		}
	}
	
	private final static Empty<?> emptyRef = new Empty<Object>();
	
	@SuppressWarnings("unchecked")
	public static <S> Empty<S> empty() {
		return (Empty<S>) emptyRef;
	}
	
	public static class ArgMapping<T> extends Dynamic<T> {
		protected final int first, last;
		protected Object value;
		protected List<String> names;
		
		public ArgMapping(int first, int count, int index) {
			super("...", index);
			this.first = first;
			this.last = first + count - 1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Channel<T> get(Stack stack) {
			return (Channel<T>) stack.top().get(index);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void create(Stack stack) {
			stack.top().set(index, value = new VarArgChannel<T>(stack.top(), first, last));
			if (CompilerSettings.DEBUG) {
				((VarArgChannel<T>) value).setNames(names);
			}
		}
		
		@Override
		public void check(Stack stack) {
			@SuppressWarnings("unchecked")
			VarArgChannel<T> c = (VarArgChannel<T>) stack.top().get(index);
			if (c.argSize() < names.size()) {
				throw new IllegalArgumentException("Missing argument '" + names.get(c.argSize()) + "'");
			}
		}
		
		public void clean(Stack stack) {
		    stack.top().set(index, null);
		}
		
		public void setNamesP(List<Param> l) {
			names = new ArrayList<String>();
			for (Param p : l) {
				names.add(p.name);
			}
		}
		
		public void setNamesV(List<Var> l) {
			names = new ArrayList<String>();
			for (Var v : l) {
				names.add(v.name);
			}
		}
		
		public void setNamesS(List<String> l) {
            names = l;
        }

		@Override
		public String toString() {
			if (value != null) {
				return value.toString();
			}
			else {
				return "[]";
			}
		}
	}
	
	public static class ArgMappingFuture<T> extends ArgMapping<T> {
		
		public ArgMappingFuture(int first, int count, int index) {
			super(first, count, index);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void create(Stack stack) {
			stack.top().set(index, value = new VarFutureArgChannel<T>(stack.top(), first, last));
			if (CompilerSettings.DEBUG) {
				((VarArgChannel<T>) value).setNames(names);
			}
		}
	}
	
	public static class ArgMappingFixed<T> extends ArgMapping<T> {
		public ArgMappingFixed(int first, int count, int index) {
			super(first, count, index);
			if (first + count > 32768) {
			    throw new IndexOutOfBoundsException("Frame size exceeded");
			}
		}

		@Override
		public void create(Stack stack) {
		    FixedArgChannel<T> c;
		    if (CompilerSettings.DEBUG) {
		        c = new FixedArgChannelDebug<T>(stack.top(), first, last);
		        c.setNames(names);
		    }
		    else {
		        c = new FixedArgChannel<T>(stack.top(), first, last);
		    }
			stack.top().set(index, c);
		}
		
		@Override
		public void check(Stack stack) {
			@SuppressWarnings("unchecked")
			FixedArgChannel<T> c = (FixedArgChannel<T>) stack.top().get(index);
			if (c.size() < names.size()) {
				throw new IllegalArgumentException("Missing argument '" + names.get(c.size()) + "'");
			}
			stack.top().set(index, null);
		}
	}
	
	public static class InvalidArg extends Dynamic<Object> {
		private final Node owner;
		
		public InvalidArg(Node owner, String name, int index) {
			super(name, index);
			this.owner = owner;
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, new InvalidArgChannel(owner));
		}

		@Override
		public void set(Stack stack, Channel<Object> value) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class ArgMappingFixedFuture<T> extends ArgMapping<T> {
		public ArgMappingFixedFuture(int first, int count, int index) {
			super(first, count, index);
		}

		@Override
		public void create(Stack stack) {
			FixedFutureArgChannel<T> c = new FixedFutureArgChannel<T>(stack.top(), first, last);
			stack.top().set(index, c);
			if (CompilerSettings.DEBUG) {
				c.setNames(names);
			}
		}
	}

	
	public static class Redirect<T> extends Dynamic<T> {
		protected final ChannelRef<T> dst;
		
		public Redirect(String name, int srcIndex, ChannelRef<T> dst) {
			super(name, srcIndex);
			this.dst = dst;
			if (dst == null) {
				throw new IllegalArgumentException("Redirect to null channel");
			}
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, dst.get(stack));
			if (CompilerSettings.DEBUG) {
				stack.top().setName(index, name);
			}
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void close(Stack stack) {
			Channel<T> c = get(stack);
			if (c != null) {
				c.close();
			}
		}
	}
	
	public static class Ordered<T> extends Redirect<T> {
		private final int prevIndex;
		
		public Ordered(String name, int srcIndex, ChannelRef<T> dst, Ordered<T> prev) {
			super(name, srcIndex, dst);
			if (prev == null) {
				prevIndex = -1;
			}
			else {
				prevIndex = prev.index;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void create(Stack stack) {
			if (prevIndex == -1) {
				stack.top().set(index, 
						new OrderedParallelChannel<T>(dst.get(stack), null));
			}
			else {
				stack.top().set(index, 
						new OrderedParallelChannel<T>(dst.get(stack), 
								(OrderedParallelChannel<T>) stack.top().get(prevIndex)));
			}
		}
	}
	
	public static class OrderedFramed<T> extends Redirect<T> {
		public OrderedFramed(String name, int srcIndex, ChannelRef<T> dst) {
			super(name, srcIndex, dst);
		}

		@SuppressWarnings("unchecked")
		public void create(Stack stack, Stack prev) {
			OrderedParallelChannel<T> pc = null;
			if (prev != null) {
				pc = (OrderedParallelChannel<T>) prev.top().get(index);
			}
			stack.top().set(index, new OrderedParallelChannel<T>(dst.get(stack), pc));
		}
	}
	
	public static class Buffer<T> extends Dynamic<T> {
		protected final int dstIndex;
		
		public Buffer(String name, int srcIndex, int dstIndex) {
			super(name, srcIndex);
			this.dstIndex = dstIndex;
		}

		@Override
		public void create(Stack stack) {
			stack.top().set(index, new MemoryChannel<T>());
			if (CompilerSettings.DEBUG) {
				stack.top().setName(index, name);
			}
		}

		@Override
		public void set(Stack stack, Channel<T> value) {
			throw new UnsupportedOperationException();
		}
		
		@SuppressWarnings("unchecked")
		public void commit(Stack stack) {
			((Channel<T>) stack.top().get(dstIndex)).addAll((Channel<T>) stack.top().get(index));
			stack.top().set(index, null);
		}

		@SuppressWarnings("unchecked")
		public void commit(Stack stack, Channel<T> c) {
			((Channel<T>) stack.top().get(dstIndex)).addAll(c);
		}
	}
}
