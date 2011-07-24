/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.mapping.AbstractDataNode;
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
			AbstractDataNode value = (AbstractDataNode) PA_VALUE.getValue(stack);
			
			log(leaf, value);
			    
            // TODO want to do a type check here, for runtime type checking
            // and pull out the appropriate internal value from value if it
            // is a DSHandle. There is no need (I think? maybe numerical casting?)
            // for type conversion here; but would be useful to have
            // type checking.
			
   			deepCopy(leaf, value, stack, 0);
			
			return null;
		}
		catch (FutureFault f) {
			throw f;
		}
		catch (Exception e) { // TODO tighten this
			throw new ExecutionException(e);
		}
	}

	@SuppressWarnings("unchecked")
    private void log(DSHandle leaf, DSHandle value) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Setting " + leaf + " to " + value);
	    }
	    else if (logger.isInfoEnabled()) {
	        if (leaf instanceof AbstractDataNode) {
	            AbstractDataNode data = (AbstractDataNode) leaf;
	            Path path = data.getPathFromRoot();
	            String p = path.toString();
	            if (p.equals("$"))
	                p = "";
	            String name = data.getDisplayableName() + p;
	            if (value.getType().isArray()) {
	                logger.info("Set: " + name + "=" + 
                                unpackHandles(value.getArrayValue()));
	            }
	            else {
	                logger.info("Set: " + name + "=" + value.getValue());
	            }
	        }
	    }
    }

	String unpackHandles(Map<Comparable<?>, DSHandle> handles) { 
	    StringBuilder sb = new StringBuilder();
	    sb.append("{");
	    Iterator<Map.Entry<Comparable<?>, DSHandle>> it = 
	        handles.entrySet().iterator();
	    while (it.hasNext()) { 
	        Map.Entry<Comparable<?>, DSHandle> entry = it.next();
	        sb.append(entry.getKey());
	        sb.append('=');
	        sb.append(entry.getValue().getValue());
	        if (it.hasNext())
	            sb.append(", ");
	    }
	    sb.append("}");
	    return sb.toString();
	}
	
    /** make dest look like source - if its a simple value, copy that
	    and if its an array then recursively copy */
	void deepCopy(DSHandle dest, DSHandle source, VariableStack stack, int level) throws ExecutionException {
	    ((AbstractDataNode) source).waitFor();
		if (source.getType().isPrimitive()) {
			dest.setValue(source.getValue());
		}
		else if (source.getType().isArray()) {
			PairIterator it;
			if (stack.isDefined("it" + level)) {
			    it = (PairIterator) stack.getVar("it" + level);
			}
			else {
			    it = new PairIterator(source.getArrayValue());
			    stack.setVar("it" + level, it);
			}
			while (it.hasNext()) {
				Pair pair = (Pair) it.next();
				Comparable<?> lhs = (Comparable<?>) pair.get(0);
				DSHandle rhs = (DSHandle) pair.get(1);
				Path memberPath = Path.EMPTY_PATH.addLast(lhs, true);
				DSHandle field;
				try {
					field = dest.getField(memberPath);
				}
				catch (InvalidPathException ipe) {
					throw new ExecutionException("Could not get destination field", ipe);
				}
				deepCopy(field, rhs, stack, level + 1);
			}
			stack.currentFrame().deleteVar("it" + level);
			dest.closeShallow();
		} 
		else if (!source.getType().isComposite()) {
		    Path dpath = dest.getPathFromRoot();
		    if (dest.getMapper().canBeRemapped(dpath)) {
		        if (logger.isDebugEnabled()) {
		            logger.debug("Remapping " + dest + " to " + source);
		        }
		        
		        dest.getMapper().remap(dpath, source.getMapper(), source.getPathFromRoot());

		        dest.closeShallow();
		    }
		    else {
		        if (stack.currentFrame().isDefined("fc")) {
		            FileCopier fc = (FileCopier) stack.currentFrame().getVar("fc");
		            if (!fc.isClosed()) {
		                throw new FutureNotYetAvailable(fc);
		            }
		            else {
		                if (fc.getException() != null) {
		                    throw new ExecutionException("Failed to copy " + source + " to " + dest, fc.getException());
		                }
		            }
		            dest.closeShallow();
		        }
		        else {
		            FileCopier fc = new FileCopier(source.getMapper().map(source.getPathFromRoot()), 
		                dest.getMapper().map(dpath));
		            stack.setVar("fc", fc);
		            try {
		                fc.start();
		            }
		            catch (Exception e) {
		                throw new ExecutionException("Failed to start file copy", e);
		            }
		            throw new FutureNotYetAvailable(fc);
		        }
		    }
		}
		else {
		    // TODO implement this
            //throw new RuntimeException("Deep non-array structure copying not implemented, when trying to copy "+source);
		}
	}

}
