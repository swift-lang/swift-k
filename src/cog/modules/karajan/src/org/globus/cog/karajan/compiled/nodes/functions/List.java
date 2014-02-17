// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.util.ArrayList;
import java.util.Collection;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Param;

public class List {
	
	public static class Size extends AbstractSingleValuedFunction {
		private ArgRef<Collection<Object>> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			return list.getValue(stack).size();
		}
	}
	
	public static class Prepend extends AbstractSingleValuedFunction {
		private ArgRef<java.util.List<Object>> list;
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Param[] getParams() {
			return params("list", "...");
		}
		
		@Override
		public Object function(Stack stack) {
			java.util.List<Object> l = list.getValue(stack);
			for (Object o : c_vargs.get(stack)) {
				l.add(0, o);
			}
			return l;
		}
	}
	
	public static class Join extends AbstractSingleValuedFunction {
		private ChannelRef<Collection<Object>> c_vargs;
		
		@Override
		protected Param[] getParams() {
			return params("...");
		}
		
		@Override
		public Object function(Stack stack) {
			ArrayList<Object> lr = new ArrayList<Object>();
			for (Collection<Object> l : c_vargs.get(stack)) {
				lr.addAll(l);
			}
			return lr;
		}
	}
	
	public static class Append extends AbstractSingleValuedFunction {
		private ArgRef<Collection<Object>> list;
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Param[] getParams() {
			return params("list", "...");
		}
		
		@Override
		public Object function(Stack stack) {
			Collection<Object> l = list.getValue(stack);
			for (Object o : c_vargs.get(stack)) {
				l.add(o);
			}
			return l;
		}
	}	
	
	public static class Cons extends AbstractSingleValuedFunction {
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Param[] getParams() {
			return params("...");
		}

		public Object function(Stack stack) {
			java.util.List<Object> l = c_vargs.get(stack).getAll();
			if (l.isEmpty()) {
			    return new ArrayList<Object>();
			}
			else {
			    return l;
			}
		}
	}
	
	public static class ButLast extends AbstractSingleValuedFunction {
		private ArgRef<java.util.List<Object>> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			java.util.List<Object> l = list.getValue(stack);
			return l.subList(0, l.size() - 1);
		}
	}
	
	public static class ButFirst extends AbstractSingleValuedFunction {
		private ArgRef<Object> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			Object o = list.getValue(stack);
			if (o instanceof Channel) {
				return ((Channel<?>) o).subChannel(1);
			}
			else if (o instanceof java.util.List) {
				@SuppressWarnings("unchecked")
				java.util.List<Object> l = (java.util.List<Object>) o;
				return l.subList(1, l.size());
			}
			throw new ExecutionException(this, "Not a list: " + o);
		}
	}
	
	public static class Last extends AbstractSingleValuedFunction {
		private ArgRef<java.util.List<Object>> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			java.util.List<Object> l = list.getValue(stack);
			return l.get(l.size() - 1);
		}
	}
	
	public static class First extends AbstractSingleValuedFunction {
		private ArgRef<Object> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			Object o = list.getValue(stack);
			if (o instanceof java.util.List) {
				return ((java.util.List<?>) o).get(0);
			}
			else if (o instanceof Channel) {
				return ((Channel<?>) o).get(0);
			}
			throw new ExecutionException(this, "Not a list: " + o);
		}
	}
	
	
	public static class Get extends AbstractSingleValuedFunction {
		private ArgRef<Object> list;
		private ArgRef<Number> index;
		
		@Override
		protected Param[] getParams() {
			return params("list", "index");
		}
		
		@Override
		public Object function(Stack stack) {
			Object o = list.getValue(stack);
			int index = this.index.getValue(stack).intValue();
			if (index < 1) {
				throw new ExecutionException(this, "index < 1");
			}
			index--;
			if (o instanceof java.util.List) {
				return ((java.util.List<?>) o).get(index);
			}
			else if (o instanceof Channel) {
				return ((Channel<?>) o).get(index);
			}
			throw new ExecutionException(this, "Not a list: " + o);
		}
	}
	

	public static class IsEmpty extends AbstractSingleValuedFunction {
		private ArgRef<Object> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			Object o = list.getValue(stack);
			if (o instanceof java.util.List) {
				return ((java.util.List<?>) o).isEmpty();
			}
			else if (o instanceof Channel) {
				return ((Channel<?>) o).isEmpty();
			}
			throw new ExecutionException(this, "Not a list: " + o);
		}
	}
	
	public static class IsList extends AbstractSingleValuedFunction {
		private ArgRef<Object> list;
		
		@Override
		protected Param[] getParams() {
			return params("list");
		}
		
		@Override
		public Object function(Stack stack) {
			Object o = list.getValue(stack);
			if (o instanceof java.util.List) {
				return true;
			}
			else if (o instanceof Channel) {
				return true;
			}
			else {
				return false;
			}
		}
	}
}