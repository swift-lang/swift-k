/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.karajan.FuturePairIterator;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.karajan.VDL2FutureException;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class SetFieldValue extends VDLFunction {
	public static final Logger logger = Logger.getLogger(SetFieldValue.class);

	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments(SetFieldValue.class, new Arg[] { OA_PATH, PA_VAR, PA_VALUE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle leaf = var.getField(path);
			DSHandle value = (DSHandle)PA_VALUE.getValue(stack);
			if (logger.isInfoEnabled()) {
				logger.info("Setting " + leaf + " to " + value);
			}
			synchronized (var.getRoot()) {
// TODO want to do a type check here, for runtime type checking
// and pull out the appropriate internal value from value if it
// is a DSHandle. There is no need (I think? maybe numerical casting?)
// for type conversion here; but would be useful to have
// type checking.
				synchronized(value.getRoot()) {
					if(!value.isClosed()) {
						throw new FutureNotYetAvailable(addFutureListener(stack, value));
					}
					deepCopy(leaf,value,stack);
				}
			}
			return null;
		}
		catch (FutureNotYetAvailable fnya) {
			throw fnya;
		}
		catch (Exception e) { // TODO tighten this
			throw new ExecutionException(e);
		}
	}

	/** make dest look like source - if its a simple value, copy that
	    and if its an array then recursively copy */
	void deepCopy(DSHandle dest, DSHandle source, VariableStack stack) throws ExecutionException {
		if(source.getType().isPrimitive()) {
			dest.setValue(source.getValue());
		} else if(source.getType().isArray()) {
			PairIterator it = new PairIterator(source.getArrayValue());
			while(it.hasNext()) {
				Pair pair = (Pair) it.next();
				Object lhs = pair.get(0);
				DSHandle rhs = (DSHandle) pair.get(1);
				Path memberPath = Path.EMPTY_PATH.addLast(String.valueOf(lhs),true);
				DSHandle field;
				try {
					field = dest.getField(memberPath);
				} catch(InvalidPathException ipe) {
					throw new ExecutionException("Could not get destination field",ipe);
				}
				deepCopy(field,rhs,stack);
			}
			closeShallow(stack, dest);
		} else {
			// TODO implement this
			throw new RuntimeException("Deep non-array structure copying not implemented, when trying to copy "+source);
		}
	}

}
