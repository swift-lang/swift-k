/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;



public class SliceArray extends VDLFunction {

        public static final Arg PA_TYPE = new Arg.Positional("type");

	static {
		setArguments(SliceArray.class, new Arg[] { PA_VAR, PA_PATH, PA_TYPE });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR.getValue(stack);

// TODO for now, this insists the the array be closed entirely before we
// execute. This may cause overserialisation; and worse, will break when
// we are trying to use the cut as an output parameter, not an input
// parameter (likely resulting in a hang).
// Need to think hard about how to handle this. Static assignment
// analysis is going to fail, I think - its like pointer aliasing in C,
// a bit. If I get a ref to an array element using this, then I can
// assign to it, but the compiler isn't going to be aware that I am
// assigning to it so can't construct partialCloseDatasets correctly...
// perhaps thats another argument for map? (as in, moving away from
// [] based assignments...


		if(var1 instanceof DSHandle) {

			try {
				AbstractDataNode sourceArray = (AbstractDataNode) var1;
				sourceArray.waitFor();

				Type sourceType = sourceArray.getType();

				if(!sourceType.isArray()) {
					throw new RuntimeException("SliceArray can only slice arrays.");
				}

				String destinationTypeName = (String) PA_TYPE.getValue(stack);
				Type destinationType = Types.getType(destinationTypeName);
				RootArrayDataNode destinationArray = new RootArrayDataNode(destinationType);


                               	Path cutPath = Path.EMPTY_PATH.addLast((String)PA_PATH.getValue(stack), false);

                        	PairIterator it = new PairIterator(sourceArray.getArrayValue());

				while(it.hasNext()) {
					Pair pair = (Pair) it.next();
					Object index = pair.get(0);
					DSHandle sourceElement = (DSHandle) pair.get(1);


                                	Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);

					DSHandle n = sourceElement.getField(cutPath);

                                	destinationArray.getField(p).set((DSHandle) n);
				}

				// all of the inputs should be closed, so
				// we only need shallow close
				destinationArray.closeShallow();

				return destinationArray;

/* code from setfieldvalue to look at:
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

*/
			}
			catch(NoSuchTypeException nste) {
				throw new ExecutionException("No such type",nste);
			}
			catch (InvalidPathException e) {
				throw new ExecutionException(e);
			}
		} else {
			throw new ExecutionException("was expecting a DSHandle or collection of DSHandles, got: "+var1.getClass());
		}
	}


}
