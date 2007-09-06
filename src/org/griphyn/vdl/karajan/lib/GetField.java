/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class GetField extends VDLFunction {
	static {
		setArguments(GetField.class, new Arg[] { OA_PATH, PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);


		if(var1 instanceof DSHandle) {

			try {
				DSHandle var = (DSHandle) var1;
				Path path = parsePath(OA_PATH.getValue(stack), stack);
				Collection fields = var.getFields(path);
				if(fields.size() == 1) {
					return fields.iterator().next();
				} else {
					return fields;
				}
			}
			catch (InvalidPathException e) {
				throw new ExecutionException(e);
			}
			catch (HandleOpenException e) {
				throw new ExecutionException(e);
			}
		} else if (var1 instanceof Collection) {
			// this path gets reached if we've been passed the results
			// of a [*] array reference
			// iterate over each element in the collection, performing the
			// above code on each; and then merge the resulting collections
			// into one before performing the return processing
			Collection var = (Collection)var1;
			Collection results = new ArrayList();
			Iterator i = var.iterator();
			try {
				Path path = parsePath(OA_PATH.getValue(stack), stack);
				while(i.hasNext()) {
					DSHandle d = (DSHandle) i.next();
					Collection theseResults = d.getFields(path);
					results.addAll(theseResults);
				}
			}
			catch (InvalidPathException e) {
				throw new ExecutionException(e);
			}
			catch (HandleOpenException e) {
				throw new ExecutionException(e);
			}
			if(results.size() == 1) {
				return results.iterator().next();
			} else {
				return results;
			}
		} else {
			throw new ExecutionException("was expecting a DSHandle or collection of DSHandles, got: "+var1.getClass());
		}
	}


}
