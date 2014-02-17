//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 8, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.List;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;


public class DynamicScope extends Scope {
	
	public DynamicScope(WrapperNode owner, Scope scope) {
		super(owner, scope);
	}
	
	@Override
	protected Var lookupParamRecursive(String name, Node src) {
		Var v = super.lookupParamRecursive(name, src);
		return new DynamicWrapper(v);
	}

	@Override
	protected Var.Channel lookupChannelRecursive(String name, Node src) {
		Var.Channel c = super.lookupChannelRecursive(name, src);
		return new DynamicChannelWrapper(c);
	}
	
	@Override
	protected Var lookup(String var, int frame) {
		return parent.lookup(var, frame);
	}

	@Override
	protected <T> VarRef<T> getVarRef(String name, int frame) {
		return parent.getVarRef(name, frame);
	}

	@Override
	protected <T> ChannelRef<T> getChannelRefRecursive(String name, int frame) {
		return parent.getChannelRefRecursive(name, frame);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	protected String getType() {
		return "D";
	}

	private static class DynamicChannelWrapper extends Var.Channel {
		private final Var.Channel c;
		
		public DynamicChannelWrapper(Var.Channel c) {
			super(c.getChannelName());
			this.c = c;
		}

		@Override
		public boolean append(Object o) {
			c.appendDynamic();
			return false;
		}

		@Override
		public StaticChannel getChannel() {
			return c.getChannel();
		}

		@Override
		public List<Object> getAll() {
			return c.getChannel().getAll();
		}

		@Override
		public int size() {
			return c.size();
		}

		@Override
		public Object getValue() {
			return c.getValue();
		}

		@Override
		public void setValue(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setIndex(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getFrame() {
			return c.getFrame();
		}

		@Override
		public int getIndex() {
			return c.getIndex();
		}

		@Override
		public void setDynamic() {
			c.setDynamic();
		}

		@Override
		public boolean isDynamic() {
			return true;
		}

		@Override
		public boolean getCanBeNull() {
			return c.getCanBeNull();
		}

		@Override
		public void setCanBeNull(boolean canBeNull) {
			c.setCanBeNull(canBeNull);
		}

		@Override
		public String toString() {
			return "WC[" + c + "]";
		}
	}

	private static class DynamicWrapper extends Var {
		private final Var v;
		
		public DynamicWrapper(Var v) {
			super(v.name);
			this.v = v;
		}

		@Override
		public Object getValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setValue(Object value) {
			v.setDynamic();
		}

		@Override
		public void setIndex(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDynamic() {
			v.setDynamic();
		}

		@Override
		public boolean isDynamic() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getCanBeNull() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCanBeNull(boolean canBeNull) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getFrame() {
			return v.getFrame();
		}

		@Override
		public int getIndex() {
			return v.getIndex();
		}

		@Override
		public String toString() {
			return "DW[" + v + "]";
		}
	}
}
