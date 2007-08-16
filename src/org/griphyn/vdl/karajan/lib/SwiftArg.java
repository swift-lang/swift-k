/*
 * Created on Jul 31, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.DSHandle;

public abstract class SwiftArg extends Arg {
	public static final Logger logger = Logger.getLogger(SwiftArg.class);

	public SwiftArg(String name, int index) {
		super(name, index);
	}

	public SwiftArg(String name) {
		super(name, IMPLICIT);
	}

	protected Object unwrap(VariableStack stack, Object val) throws ExecutionException {
		if (val instanceof DSHandle) {
			DSHandle handle = (DSHandle) val;
			if (handle.isArray()) {
				Map value = handle.getArrayValue();
				if (handle.isClosed()) {
					return new PairIterator(value);
				}
				else {
					return VDLFunction.addFutureListListener(stack, handle, value);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("SwiftArg.getValue(" + handle + ")");
			}
			synchronized (handle) {
				if (!handle.isClosed()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Waiting for " + handle);
					}
					throw new FutureNotYetAvailable(VDLFunction.addFutureListener(stack, handle));
				}
				else {
					return handle.getValue();
				}
			}
		}
		else {
			throw new ExecutionException("Expected Swift data, but got some primitive type (" + val
					+ ")");
		}
	}

	public Object getValue(VariableStack stack) throws ExecutionException {
		Object v = super.getValue(stack);
		if(v == null) return v;
			else return unwrap(stack, super.getValue(stack));
	}

	public double getDoubleValue(VariableStack stack) throws ExecutionException {
		Object dbl = getValue(stack);
		if (dbl instanceof Double) {
			return ((Double) dbl).doubleValue();
		}
		else {
			throw new ExecutionException("Type error. Expected a float or int but got a "
					+ getType(stack));
		}
	}

	protected String getType0(Object o) throws ExecutionException {
		if (o instanceof DSHandle) {
			DSHandle handle = (DSHandle) o;
			return handle.getType();
		}
		else {
			throw new ExecutionException("Expected Swift data, but got some primitive type (" + o
					+ ")");
		}
	}

	public String getType(VariableStack stack) throws ExecutionException {
		return getType0(super.getValue(stack));
	}

	public static class Positional extends SwiftArg {
		public Positional(String name, int index) {
			super(name, index);
		}

		public Positional(String name) {
			super(name);
		}
	}

	public static class Optional extends SwiftArg {
		private final Object defaultValue;
		private final String defaultType;

		public Optional(String name, Object defaultValue, String defaultType) {
			super(name, NOINDEX);
			this.defaultValue = defaultValue;
			this.defaultType = defaultType;
		}

		public Optional(String name) {
			this(name, null, "any");
		}

		public Object getValue(VariableStack stack) throws ExecutionException {
			Object o = super.getValue(stack);
			if (o == null) {
				return defaultValue;
			}
			else {
				return o;
			}
		}

		public String getType(VariableStack stack) throws ExecutionException {
			Object o = super.getValue(stack, defaultValue);
			if (o == null) {
				return defaultType;
			}
			else {
				return getType0(o);
			}
		}
	}
	
	public static final class Vargs extends SwiftArg {
		public Vargs() {
			super("...");
		}

		public Object[] asArray(VariableStack stack) throws ExecutionException {
			VariableArguments args = get(stack);
			Object[] ret = new Object[args.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = unwrap(stack, args.get(i));
			}
			return ret;
		}

		public List asList(VariableStack stack) throws ExecutionException {
			VariableArguments args = get(stack);
			List ret = new ArrayList();
			Iterator i = args.iterator();
			while (i.hasNext()) {
				ret.add(unwrap(stack, i.next()));
			}
			return get(stack).getAll();
		}

		public VariableArguments get(VariableStack stack) throws ExecutionException {
			VariableArguments args = ArgUtil.getVariableArguments(stack);
			if (args == null) {
				throw new ExecutionException("No default channel found on stack");
			}
			return args;
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
