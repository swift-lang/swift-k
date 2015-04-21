/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.griphyn.vdl.karajan.PairSet;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.RootFutureArrayDataNode;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;



public class SliceArray extends SwiftFunction {
    private ArgRef<AbstractDataNode> var;
    private ArgRef<String> path;
    private ArgRef<String> type;
    private VarRef<Types> types;

	@Override
    protected Signature getSignature() {
        return new Signature(params("var", "path", "type"));
    }

	@Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        types = scope.getVarRef("#types");
    }




    @Override
    public Object function(Stack stack) {
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
    
    
    	try {
    		AbstractDataNode sourceArray = this.var.getValue(stack);
    		sourceArray.waitFor(this);
    
    		Type sourceType = sourceArray.getType();
    
    		if(!sourceType.isArray()) {
    			throw new RuntimeException("SliceArray can only slice arrays.");
    		}
    
    		String destinationTypeName = this.type.getValue(stack);
    		Type destinationType = types.getValue().getType(destinationTypeName);
    		RootFutureArrayDataNode destinationArray = new RootFutureArrayDataNode(sourceArray.getField(), null);
    
    
    		Path cutPath = Path.EMPTY_PATH.addLast(this.path.getValue(stack), false);
    
    		PairSet s = new PairSet(sourceArray.getArrayValue());
    
    		for (List<?> pair : s) {
    			Object index = pair.get(0);
    			DSHandle sourceElement = (DSHandle) pair.get(1);
        
    			DSHandle n = sourceElement.getField(cutPath);
    			
    			destinationArray.addField((Comparable<?>) index, n);
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
            throw new ExecutionException(this, e);
        }
	}
}
