//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 18, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.List;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public abstract class Arg {
	public static final int NOINDEX = -1;
	public static final int CHANNEL = -2;
	public static final int IMPLICIT = -3;

	private final String name;
	private final int index;

	public Arg(String name, int index) {
		this.name = name.toLowerCase();
		this.index = index;
	}

	public Arg(String name) {
		this(name, 0);
	}

	public final String getName() {
		return name;
	}

	public final int getIndex() {
		return index;
	}

	public final boolean hasIndex() {
		return index != NOINDEX;
	}

	protected final NamedArguments getNamed(VariableStack stack) throws ExecutionException {
		NamedArguments args = ArgUtil.getNamedArguments(stack);
		if (args == null) {
			throw new ExecutionException("No named arguments on current frame");
		}
		return args;
	}

	public boolean isPresent(VariableStack stack) throws ExecutionException {
		return getNamed(stack).hasArgument(name);
	}

	public Object getValue(VariableStack stack) throws ExecutionException {
		return getValue0(stack);
	}

	private Object getValue0(VariableStack stack) throws ExecutionException {
		Object value = getNamed(stack).getArgument(name);
		if (value instanceof Future) {
			return ((Future) value).getValue();
		}
		else {
			return value;
		}
	}

	public final Object getValue(VariableStack stack, Object defaultValue)
			throws ExecutionException {
		Object value = getValue0(stack);
		if (value == null) {
			if (isPresent(stack)) {
				return null;
			}
			else {
				return defaultValue;
			}
		}
		else {
			return value;
		}
	}

	public Object getStatic(FlowElement node) {
		return node.getStaticArguments().get(name);
	}

	public final Object getStatic(FlowElement node, Object def) {
		Object val = node.getStaticArguments().get(name);
		if (val == null) {
			return def;
		}
		else {
			return val;
		}
	}

	public final void setStatic(FlowElement node, Object value) {
		node.addStaticArgument(name, value);
	}

	public final void setStatic(FlowElement node, boolean value) {
		node.addStaticArgument(name, Boolean.valueOf(value));
	}

	public final boolean isPresentStatic(FlowElement node) {
		return node.getStaticArguments().containsKey(name);
	}

	public String toString() {
		return name;
	}

	public static final class Positional extends Arg {
		public Positional(String name, int index) {
			super(name, index);
		}

		public Positional(String name) {
			super(name, IMPLICIT);
		}

		public Object getValue(VariableStack stack) throws ExecutionException {
			Object value = super.getValue(stack);
			if (value == null) {
				if (isPresent(stack)) {
					return null;
				}
				else {
					throw new ExecutionException("Missing argument " + getName());
				}
			}
			else {
				return value;
			}
		}
	}

	public static final class TypedPositional extends Arg {
		private final Class cls;
		private final String type;

		public TypedPositional(String name, int index, Class cls, String type) {
			super(name, index);
			this.cls = cls;
			this.type = type;
		}

		public TypedPositional(String name, Class cls, String type) {
			this(name, IMPLICIT, cls, type);
		}

		public TypedPositional(String name, Class cls) {
			this(name, IMPLICIT, cls, cls.getName());
		}

		public Object getValue(VariableStack stack) throws ExecutionException {
			Object value = super.getValue(stack);
			if (value == null) {
				if (isPresent(stack)) {
					return null;
				}
				else {
					throw new ExecutionException("Missing argument " + getName());
				}
			}
			else {
				if (!cls.isAssignableFrom(value.getClass())) {
					String vc;
					if (value instanceof FlowElement) {
						vc = ((FlowElement) value).getElementType();
					}
					else {
						vc = value.getClass().getName();
					}
					throw new ExecutionException("Incompatible argument type for " + getName()
							+ ": expected " + type + "; got " + vc + ". Offending argument: "
							+ value);
				}
				return value;
			}
		}
	}

	public static final class Optional extends Arg {
		private final Object defaultValue;

		public Optional(String name, Object defaultValue) {
			super(name, NOINDEX);
			this.defaultValue = defaultValue;
		}

		public Optional(String name) {
			this(name, null);
		}

		public Object getValue(VariableStack stack) throws ExecutionException {
			return super.getValue(stack, defaultValue);
		}

		public Object getStatic(FlowElement node) {
			Object res = super.getStatic(node);
			if (res == null) {
				return defaultValue;
			}
			else {
				return res;
			}
		}
	}

	public static class Channel extends Arg {
		private transient String variableName;

		public Channel(String name) {
			super(name, CHANNEL);
			this.variableName = variableName(getName());
		}

		public VariableArguments get(VariableStack stack) throws ExecutionException {
			return ArgUtil.getChannelArguments(stack, this);
		}

		public VariableArguments getReturn(VariableStack stack) throws ExecutionException {
			return ArgUtil.getChannelReturn(stack, this);
		}

		public void create(VariableStack stack, VariableArguments value) {
			ArgUtil.createChannel(stack, this, value);
		}

		public void create(VariableStack stack) {
			ArgUtil.createChannel(stack, this);
		}

		public void ret(VariableStack stack, Object value) throws VariableNotFoundException {
			ArgUtil.getChannelReturn(stack, this).append(value);
		}

		public String getVariableName() {
			if (variableName == null) {
				variableName = variableName(getName());
			}
			return variableName;
		}

		public boolean isPresent(VariableStack stack) throws ExecutionException {
			return ArgUtil.getChannelArguments(stack, this) != null;
		}

		public static String variableName(String channelName) {
			return "#channel#" + channelName;
		}

		public String toString() {
			return getName() + "||";
		}

		public boolean equals(Object obj) {
			if (obj instanceof Channel) {
				return ((Channel) obj).getName().equals(getName());
			}
			return false;
		}

		public int hashCode() {
			return getName().hashCode();
		}
	}

	public static final class Vargs extends Channel {
		public Vargs() {
			super("...");
		}

		public Object[] asArray(VariableStack stack) throws ExecutionException {
			VariableArguments args = get(stack);
			Object[] ret = new Object[args.size()];
			for (int i = 0; i < ret.length; i++) {
				Object obj = args.get(i);
				if (obj instanceof Future) {
					ret[i] = ((Future) obj).getValue();
				}
				else {
					ret[i] = obj;
				}
			}
			return ret;
		}

		public List asList(VariableStack stack) throws ExecutionException {
			return get(stack).getAll();
		}

		public VariableArguments get(VariableStack stack) throws ExecutionException {
			VariableArguments args = ArgUtil.getVariableArguments(stack);
			if (args == null) {
				throw new ExecutionException("No default channel found on stack");
			}
			return args;
		}

		public VariableArguments getReturn(VariableStack stack) throws ExecutionException {
			VariableArguments args = ArgUtil.getVariableReturn(stack);
			if (args == null) {
				throw new ExecutionException("No default channel found on stack");
			}
			return args;
		}

		public void create(VariableStack stack) {
			ArgUtil.initializeVariableArguments(stack);
		}

		public boolean isPresent(VariableStack stack) throws ExecutionException {
			return ArgUtil.getVariableArguments(stack) != null;
		}

		public String getVariableName() {
			return "#vargs";
		}

	}

	public static final Vargs VARGS = new Vargs();
}
