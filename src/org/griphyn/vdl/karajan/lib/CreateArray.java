package org.griphyn.vdl.karajan.lib;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.type.Type;

public class CreateArray extends VDLFunction {

	public static final Logger logger = Logger.getLogger(CreateArray.class);

	public static final Arg PA_VALUE = new Arg.Positional("value");
	
	static {
		setArguments(CreateArray.class, new Arg[] { PA_VALUE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Object value = PA_VALUE.getValue(stack);
		try {

			if (!(value instanceof List)) {
				throw new RuntimeException(
					"An array variable can only be initialized with a list of values");
			}

			Type type = null;

			Iterator i = ((List) value).iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (o instanceof DSHandle) {
					DSHandle d = (DSHandle)o;
					Type thisType = d.getType();
					if(type == null) {
						// this first element
						type = thisType;
					} else {
						// other elements, when we have a type to expect
						if(!(type.equals(thisType))) {
							throw new RuntimeException(
								"Expecting all array elements to have SwiftScript type "+type+" but found an element with type "+thisType);
						}
					}
				}
				else {
					throw new RuntimeException("An array variable can only be initialized by a list of DSHandle values.");
				}

			}

			DSHandle handle = new RootArrayDataNode(type.arrayType());

			logger.info("CREATEARRAY START array="+handle.getIdentifier());

			int index = 0;
			i = ((List) value).iterator();
			while (i.hasNext()) {
				// TODO check type consistency of elements with
				// the type of the array
				DSHandle n = (DSHandle) i.next();
				// we know this DSHandle cast will work because we checked
				// it in the previous scan of the array contents
				Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);

				handle.getField(p).set(n);
				logger.info("CREATEARRAY MEMBER array="+handle.getIdentifier()+" index="+index+" member="+n.getIdentifier());
				index++;
			}
			closeShallow(stack, handle);
			logger.info("CREATEARRAY COMPLETED array="+handle.getIdentifier());

			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

}
