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


package org.griphyn.vdl.karajan.lib;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.FutureFault;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.type.Type;

public class CreateArray extends SetFieldValue {
	public static final Logger logger = Logger.getLogger(CreateArray.class);
	
	private ArgRef<Object> value;

	@Override
    protected Signature getSignature() {
        return new Signature(params("value", optional("_traceline", null)));
    }

    public Object function(Stack stack) {
		Object value = this.value.getValue(stack);
		try {

			if (!(value instanceof List)) {
				throw new RuntimeException("An array variable can only be initialized with a list of values");
			}

			Type type = checkTypes((List<?>) value);
			
			RootHandle handle = new RootArrayDataNode("arrayexpr", type.arrayType());
			if (type.hasNonPrimitiveComponents()) {
			    handle.init(new ConcurrentMapper());
			}
			else {
			    handle.init(null);
			}

			if (logger.isInfoEnabled()) {
			    logger.info("CREATEARRAY START array=" + handle.getIdentifier());
			}

			int index = 0;
			for (Object o : (List<?>) value) {
				// TODO check type consistency of elements with
				// the type of the array
				DSHandle n = (DSHandle) o;
				// we know this DSHandle cast will work because we checked
				// it in the previous scan of the array contents
				Path p = Path.EMPTY_PATH.addLast(index, true);
				
				DSHandle dst = handle.getField(p);

				deepCopy(dst, n, stack);
				
				if (logger.isInfoEnabled()) {
				    logger.info("CREATEARRAY MEMBER array=" + handle.getIdentifier() 
				        + " index=" + index + " member=" + n.getIdentifier());
				}
				index++;
			}
			
			handle.closeShallow();
			
			if (logger.isInfoEnabled()) {
			    logger.info("CREATEARRAY COMPLETED array=" + handle.getIdentifier());
			}

			return handle;
		}
		catch (FutureFault e) {
		    throw e;
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}

    private Type checkTypes(List<?> value) {
        Type type = null;
        
        for (Object o : value) {            
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
                            "Expecting all array elements to have SwiftScript type " + 
                            type + " but found an element with type "+thisType);
                    }
                }
            }
            else {
                throw new RuntimeException("An array variable can only be initialized by a list of DSHandle values.");
            }
        }
        return type;
    }
}
