/*
 * Created on Jul 31, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

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
			AbstractDataNode handle = (AbstractDataNode) val;
			if (logger.isDebugEnabled()) {
                logger.debug("SwiftArg.getValue(" + handle + ")");
            }
			if (handle.getType().isArray()) {
				return handle;
			}
			else {
			    handle.waitFor();
			    return handle.getValue();
			}
		}
		else {
			throw new ExecutionException("Expected Swift data, but got some primitive type (" + val
					+ ")");
		}
	}

	public Object getValue(VariableStack stack) throws ExecutionException {
		Object v = super.getValue(stack);
		if (v == null) {
		    return v;
		}
		else {
		    return unwrap(stack, super.getValue(stack));
		}
	}

	public double getDoubleValue(VariableStack stack) throws ExecutionException {
		return checkDouble(getValue(stack));
	}
	
	public static double checkDouble(Object dbl) throws ExecutionException {
		if (dbl instanceof Double) {
			return ((Double) dbl).doubleValue();
		}
		else {
			throw new ExecutionException("Internal type error. Expected a Double. Got " + classOf(dbl));
		}
	}

	public DSHandle getRawValue(VariableStack stack) throws ExecutionException {
		Object v = super.getValue(stack);
		if(v instanceof DSHandle) {
			return (DSHandle)v;
		} else if(v == null) { 
			return null;
		} else {
			throw new ExecutionException("Expected Swift data, but got some primitive type (" + v + ")");
		}
	}
	
	private static Class classOf(Object object) {
		if (object == null) {
			return null;
		}
		else {
			return object.getClass();
		}
	}

	protected Type getType0(Object o) throws ExecutionException {
		if (o instanceof DSHandle) {
			DSHandle handle = (DSHandle) o;
			return handle.getType();
		}
		else {
			throw new ExecutionException("Expected Swift data, but got some primitive type (" + o
					+ ")");
		}
	}

	public Type getType(VariableStack stack) throws ExecutionException {
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
		private final Type defaultType;

		public Optional(String name, Object defaultValue, Type defaultType) {
			super(name, NOINDEX);
			this.defaultValue = defaultValue;
			this.defaultType = defaultType;
		}

		public Optional(String name) {
			this(name, null, Types.ANY);
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

		public Type getType(VariableStack stack) throws ExecutionException {
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

		public DSHandle[] asDSHandleArray(VariableStack stack) throws ExecutionException {
			VariableArguments args = get(stack);
			DSHandle[] ret = new DSHandle[args.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = (DSHandle) args.get(i);
			}
			return ret;
		}
		
		public AbstractDataNode[] asDataNodeArray(VariableStack stack) throws ExecutionException {
            VariableArguments args = get(stack);
            AbstractDataNode[] ret = new AbstractDataNode[args.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = (AbstractDataNode) args.get(i);
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
