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
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.Pair;
import org.griphyn.vdl.karajan.PairIterator;
import org.griphyn.vdl.karajan.VDL2FutureException;
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
			DSHandle value = (DSHandle) PA_VALUE.getValue(stack);
			
			log(leaf, value);
			    
            // TODO want to do a type check here, for runtime type checking
            // and pull out the appropriate internal value from value if it
            // is a DSHandle. There is no need (I think? maybe numerical casting?)
            // for type conversion here; but would be useful to have
            // type checking.
			synchronized (value.getRoot()) {
				if (!value.isClosed()) {
					throw new FutureNotYetAvailable(addFutureListener(stack, value));
				}
			}
			try {
    			synchronized (var.getRoot()) {
    				deepCopy(leaf, value, stack);
    				if (var.getParent() != null && var.getParent().getType().isArray()) {
    				    markAsAvailable(stack, leaf.getParent(), leaf.getPathFromRoot().getLast());
    				}
    			}
			}
			catch (VDL2FutureException e) {
			    throw new FutureNotYetAvailable(addFutureListener(stack, e.getHandle()));
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
	            Object v = value.getValue();
	            if (! (v instanceof Map))
	                logger.info("Set: " + name + "=" + v);
	            else
	                logger.info("Set: " + name + "=" + 
	                            unpackHandles((Map<String, DSHandle>) v));
	        }
	    }
    }

	String unpackHandles(Map<String,DSHandle> handles) { 
	    StringBuilder sb = new StringBuilder();
	    sb.append("{");
	    Iterator<Map.Entry<String,DSHandle>> it = 
	        handles.entrySet().iterator();
	    while (it.hasNext()) { 
	        Map.Entry<String,DSHandle> entry = it.next();
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
	void deepCopy(DSHandle dest, DSHandle source, VariableStack stack) throws ExecutionException {
		if (source.getType().isPrimitive()) {
			dest.setValue(source.getValue());
		}
		else if (source.getType().isArray()) {
			PairIterator it;
			if (stack.isDefined("it")) {
			    it = (PairIterator) stack.getVar("it");
			}
			else {
			    it = new PairIterator(source.getArrayValue());
			    stack.setVar("it", it);
			}
			while (it.hasNext()) {
				Pair pair = (Pair) it.next();
				Object lhs = pair.get(0);
				DSHandle rhs = (DSHandle) pair.get(1);
				Path memberPath;
				if (lhs instanceof Double) {
				    memberPath = Path.EMPTY_PATH.addLast(String.valueOf(((Double) lhs).intValue()), true);
				}
				else {
				    memberPath = Path.EMPTY_PATH.addLast(String.valueOf(lhs), true);
				}
				DSHandle field;
				try {
					field = dest.getField(memberPath);
				}
				catch (InvalidPathException ipe) {
					throw new ExecutionException("Could not get destination field",ipe);
				}
				deepCopy(field, rhs, stack);
			}
			closeShallow(stack, dest);
		} 
		else if (!source.getType().isComposite()) {
		    Path dpath = dest.getPathFromRoot();
		    if (dest.getMapper().canBeRemapped(dpath)) {
		        if (logger.isDebugEnabled()) {
		            logger.debug("Remapping " + dest + " to " + source);
		        }
		        dest.getMapper().remap(dpath, source.getMapper().map(source.getPathFromRoot()));
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
		                throw new FutureNotYetAvailable(fc);
		            }
		            catch (FutureNotYetAvailable e) {
		                throw e;
		            }
		            catch (Exception e) {
		                throw new ExecutionException("Failed to start file copy", e);
		            }
		        }
		    }
		}
		else {
		    // TODO implement this
            //throw new RuntimeException("Deep non-array structure copying not implemented, when trying to copy "+source);
		}
	}

}
