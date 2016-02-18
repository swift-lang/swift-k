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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import k.rt.Channel;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.MemoryChannel;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.karajan.AssertFailedException;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.SwiftConfig;

public abstract class SwiftFunction extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(SwiftFunction.class);
	
	public static final boolean PROVENANCE_ENABLED = SwiftConfig.getDefault().isProvenanceEnabled();

	private VarRef<Context> context;
	
    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        context = scope.getVarRef("#context");
    }

    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        returnDynamic(scope);
        return super.compileBody(w, argScope, scope);
    }
    
    

    @Override
    public void runBody(LWThread thr) {
        Stack stack = thr.getStack();
		try {
		    ret(stack, function(stack));
		}
		catch (AssertFailedException e) { 
            logger.fatal("swift: assert failed: " + e.getMessage());
            throw e;
        }
        catch (DependentException e) {
            ret(stack, NodeFactory.newRoot(getReturnType(), e));
        }
    }
    
    protected Field getReturnType() {
        return Field.GENERIC_ANY;
    }
	
	/*
	 * This will likely break if the engine changes in fundamental ways. It also
	 * depends on the fact that iteration variable is named '$' in this
	 * particular implementation.
	 */
	public static String getThreadPrefix() {
		return getThreadPrefix(LWThread.currentThread());
	}
		
	public static String getThreadPrefix(LWThread thr) {
	    if (thr == null) {
	        return "STATIC";
	    }
	    else {
	        return thr.getQualifiedName();
	    }
	}

	// TODO - is this needed any more? its doing some type inferencing and
	// object creation and dequoting of strings, but the necessary behaviour
	// here has possibly moved elsewhere, into a more strongly typed
	// intermediate
	// XML form that removes the need for this inference.

	// we might need to do some casting here for the numerical stuff - eg when
	// asking for a float but we're given an int? not sure? might be the case
	// that we already have value in the Double form already, in which case
	// deference the internal value?

	// this is only used by VDL new (and really should only be used by
	// VDL new, and should perhaps move to the VDL new source?)

    protected Object internalValue(Type type, Object value) {
		if (Types.FLOAT.equals(type)) {
			return Double.valueOf(TypeUtil.toDouble(value));
		}
		else if (Types.INT.equals(type)) {
			return Integer.valueOf(TypeUtil.toInt(value));
		}
		else if (Types.BOOLEAN.equals(type)) {
			return new Boolean(TypeUtil.toBoolean(value));
		}
		else {
			return value;
		}
	}

	protected Object pathOnly(Object f) {
		if (f instanceof String[]) {
			return pathOnly((String[]) f);
		}
		else {
			return pathOnly((String) f);
		}
	}
	
	protected DuplicateMappingChecker getDMChecker(Stack stack) {
        Context ctx = this.context.getValue(stack);
        return (DuplicateMappingChecker) ctx.getAttribute("SWIFT:DM_CHECKER");
    }

	protected static String pathOnly(String file) {
	    AbsFile af = new AbsFile(file);
	    if ("file".equals(af.getProtocol())) {
	        return af.getPath();
	    }
	    else {
	        return af.getHost() + "/" + af.getPath();
	    }
	}

	protected String[] pathOnly(String[] files) {
		String[] p = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			p[i] = pathOnly(files[i]);
		}
		return p;
	}

	protected boolean compatible(Type expectedType, Type actualType) {
		if (expectedType.equals(Types.FLOAT)) {
			if (actualType.equals(Types.FLOAT) || actualType.equals(Types.INT)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (expectedType.equals(Types.FLOAT.arrayType())) {
			if (actualType.equals(Types.FLOAT.arrayType())
					|| actualType.equals(Types.INT.arrayType())) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (expectedType.equals(Types.ANY)) {
			return true;
		}
		else {
			return actualType.equals(expectedType);
		}
	}

	protected void closeChildren(AbstractDataNode handle) throws InvalidPathException {
		// Close the future
		handle.closeShallow();
		// Mark all leaves
		try {
            for (DSHandle child : handle.getAllFields()) {
            	child.closeShallow();
            }
        }
        catch (HandleOpenException e) {
            throw new RuntimeException("Handle not closed after closeShallow()");
        }
	}
		
	public static void waitForAll(Node who, Channel<AbstractDataNode> vargs) throws ExecutionException {
	    for (AbstractDataNode n : vargs) {
	    	n.waitFor(who);
	    }
	}
		
	public static Map<Comparable<?>, DSHandle> waitForArray(Node who, AbstractDataNode n) throws ExecutionException {
		n.waitFor(who);
		Map<Comparable<?>, DSHandle> v = n.getArrayValue();
        for (DSHandle h : v.values()) {
        	((AbstractDataNode) h).waitFor(who);
        }
        return v;
    }
		
	public static Channel<Object> unwrapAll(Node who, Channel<AbstractDataNode> vargs) throws ExecutionException {
		waitForAll(who, vargs);
		MemoryChannel<Object> mc = new MemoryChannel<Object>();
        for (AbstractDataNode n : vargs) {
            mc.add(n.getValue());
        }
        return mc;
    }
		
	public static void waitDeep(LWThread thr, AbstractDataNode n, Node who) {
	    Type t = n.getType();
	    if (!t.isComposite()) {
	        n.waitFor(who);
	    }
	    else if (t.isArray()) {
	        n.waitFor(who);
	        waitDeepForArray(thr, n, who);
	    }
	    else {
	        waitDeepForStruct(thr, n, who);
	    }
	}
	
	private static void waitDeepForStruct(LWThread thr, AbstractDataNode n, Node who) {
	    Type t = n.getType();
	    List<String> fieldNames = t.getFieldNames();
	    int i = thr.popIntState();
	    try {
    	    for (; i < fieldNames.size(); i++) {
    	        waitDeep(thr, (AbstractDataNode) n.getField(fieldNames.get(i)), who);
    	    }
	    }
	    catch (NoSuchFieldException e) {
	        throw new ExecutionException(who, 
	            "Internal error: node and type do not agree on fields ('" + 
	            fieldNames.get(i) + "'");
	    }
	    catch (Yield y) {
	        y.getState().push(i);
	        throw y;
	    }
    }

    private static void waitDeepForArray(LWThread thr, AbstractDataNode n, Node who) {
	    @SuppressWarnings("unchecked")
            Iterator<Map.Entry<Comparable<?>, DSHandle>> it = 
                (Iterator<Entry<Comparable<?>, DSHandle>>) thr.popState();
            AbstractDataNode crt = (AbstractDataNode) thr.popState();
            if (it == null) {
                it = n.getArrayValue().entrySet().iterator();
            }
            try {
                while (true) {
                    if (crt != null) {
                        waitDeep(thr, crt, who);
                    }
                    if (it.hasNext()) {
                        crt = (AbstractDataNode) it.next().getValue();
                    }
                    else {
                        break;
                    }
                }
            }
            catch (Yield y) {
                y.getState().push(crt);
                y.getState().push(it);
                throw y;
            }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unwrap(Node who, AbstractDataNode n) throws ExecutionException {
        n.waitFor(who);
        return (T) n.getValue();
    }

	public static Path parsePath(Object o) {
		if (o instanceof Path) {
			return (Path) o;
		}
		else {
			return Path.parse((String) o);
		}
	}
	
	private static int provenanceIDCount = 451000;

	public static synchronized int nextProvenanceID() {
		return provenanceIDCount++;
	}

	public static void logProvenanceResult(int id, DSHandle result, String name) {
	    if (logger.isDebugEnabled())
	        logger.debug("FUNCTION id="+id+" name="+name+" result="+result.getIdentifier());
	    else if (logger.isInfoEnabled())
	        logger.info("FUNCTION: " + name + "()");
	}

	public static void logProvenanceParameter(int id, DSHandle parameter, String paramName) {
	    if (logger.isDebugEnabled())
	        logger.debug("FUNCTIONPARAMETER id="+id+" input="+parameter.getIdentifier()+" name="+paramName);
	}
}
