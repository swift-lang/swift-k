package org.griphyn.vdl.karajan.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class CreateArray extends VDLFunction {

	public static final Logger logger = Logger.getLogger(CreateArray.class);

	public static final Arg PA_VALUE = new Arg.Positional("value");
	
	static {
		setArguments(CreateArray.class, new Arg[] { PA_VALUE });
	}

	@SuppressWarnings("unchecked")
    public Object function(VariableStack stack) throws ExecutionException {
		Object value = PA_VALUE.getValue(stack);
		try {

			if (!(value instanceof List)) {
				throw new RuntimeException(
					"An array variable can only be initialized with a list of values");
			}

			Type type = checkTypes((List<?>) value);
			
			DSHandle handle = new RootArrayDataNode(type.arrayType());
			if (hasMappableFields(type)) {
			    setMapper(handle);
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
				Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
				
				DSHandle dst = handle.getField(p);

				SetFieldValue.deepCopy(dst, n, stack, 1);
				
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
			throw new ExecutionException(e);
		}
	}

    private void setMapper(DSHandle handle) {
        // slap a concurrent mapper on this
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("descriptor", "concurrent_mapper");
        params.put("dbgname", "arrayexpr");
        handle.init(params);
    }

    private boolean hasMappableFields(Type type) {
        if (type.isPrimitive()) {
            return false;
        }
        else if (!type.isComposite()) {
            return true;
        }
        else if (type.isArray()) {
            return hasMappableFields(type.itemType());
        }
        else {
            // struct
            for (Field f : type.getFields()) {
                if (hasMappableFields(f.getType())) {
                    return true;
                }
            }
            return false;
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
