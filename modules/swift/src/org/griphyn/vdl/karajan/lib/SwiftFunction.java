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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import k.rt.Channel;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.MemoryChannel;
import k.rt.Stack;
import k.thr.LWThread;

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
import org.griphyn.vdl.mapping.GeneralizedFileFormat;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PathComparator;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
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
	    return thr.getQualifiedName();
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

	public static final String[] EMPTY_STRING_ARRAY = new String[0];


	public static String[] filename(DSHandle var) throws ExecutionException {
		try {
			if (var.getType().isArray()) {
				return leavesFileNames(var);
			}
			else if(var.getType().getFields().size() > 0) {
				return leavesFileNames(var);
			}
			else {
				return new String[] { leafFileName(var) };
			}
		}
		catch (DependentException e) {
			return new String[0];
		}
        catch (HandleOpenException e) {
            throw new ExecutionException("The current implementation should not throw this exception", e);
        }
	}

	private static String[] leavesFileNames(DSHandle var) throws ExecutionException, HandleOpenException {
	    RootHandle root = var.getRoot();
	    Mapper mapper = root.getMapper();
	    	            
        if (mapper == null) {
            throw new ExecutionException(var.getType() + " is not a mapped type");
        }
        
		List<String> l = new ArrayList<String>();
		try {
			Collection<Path> fp = var.getFringePaths();
			List<Path> src;
			if (fp instanceof List) {
				src = (List<Path>) fp;
			}
			else {
				src = new ArrayList<Path>(fp);
			}
			Collections.sort(src, new PathComparator());
			
			for (Path p : src) {
				l.add(leafFileName(var.getField(p), mapper));
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException("DSHandle is lying about its fringe paths");
		}
		return l.toArray(EMPTY_STRING_ARRAY);
	}
	
	private static String leafFileName(DSHandle var) {
	    return leafFileName(var, var.getRoot().getMapper());
	}
	
	private static String leafFileName(DSHandle var, Mapper mapper) {
		if (Types.STRING.equals(var.getType())) {
			return relativize(String.valueOf(var.getValue()));
		}
		else {
			if (mapper == null) {
				throw new ExecutionException("Cannot invoke filename() on data without a mapper: " + var);
			}
			PhysicalFormat f = mapper.map(var.getPathFromRoot());
			if (f instanceof GeneralizedFileFormat) {
				String filename = ((GeneralizedFileFormat) f).getURIAsString();
				if (filename == null) {
					throw new ExecutionException("Mapper did not provide a file name");
				}
				else {
					return filename;
				}
			}
			else if (f == null) {
				throw new ExecutionException("Mapper failed to map " + var);
			}
			else {
				throw new ExecutionException("Only file formats are supported for now");
			}
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

	/**
	 * Given an input of an array of strings, returns a single string with the
	 * input strings separated by a space. If the 'relative' flag is set to
	 * true, then each input string will be passed through the relativize
	 * function.
	 */
	public static String argList(String[] s, boolean relative) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			if (relative) {
				s[i] = relativize(s[i]);
			}
			sb.append(s[i]);
			if (i < s.length - 1) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	/**
	 * removes leading / character from a supplied filename if present, so that
	 * the path can be used as a relative path.
	 */
	public static String relativize(String name) {
		name = pathOnly(name);
		return PathUtils.remotePathName(name);
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
	
	protected static void waitFor(Node n, DSHandle h) {
        ((AbstractDataNode) h).waitFor(n);
    }
	
	public static Channel<Object> unwrapAll(Node who, Channel<AbstractDataNode> vargs) throws ExecutionException {
		waitForAll(who, vargs);
		MemoryChannel<Object> mc = new MemoryChannel<Object>();
        for (AbstractDataNode n : vargs) {
            mc.add(n.getValue());
        }
        return mc;
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
