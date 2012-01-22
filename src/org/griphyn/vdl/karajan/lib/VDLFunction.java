package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.transformation.File;
import org.globus.swift.catalog.types.TCType;
import org.griphyn.vdl.karajan.AssertFailedException;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.GeneralizedFileFormat;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.FQN;
import org.griphyn.vdl.util.VDL2ConfigProperties;

public abstract class VDLFunction extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(VDLFunction.class);

	public static final Arg.Channel ERRORS = new Arg.Channel("errors");

	public static final Arg OA_PATH = new Arg.Optional("path", "");
	public static final Arg PA_PATH = new Arg.Positional("path");
	public static final Arg PA_VAR = new Arg.Positional("var"); 
	public static final Arg OA_ISARRAY = new Arg.Optional("isArray", Boolean.FALSE);

	public final void post(VariableStack stack) throws ExecutionException {
		try {
			Object o = function(stack);
			if (o != null) {
				ret(stack, o);
			}
			super.post(stack);
		}
		catch (AssertFailedException e) { 
		    logger.fatal("swift: assert failed: " + e.getMessage());
		    stack.getExecutionContext().failedQuietly(stack, e);
		}
		catch (DependentException e) {
			// This would not be the primal fault so in non-lazy errors mode it
			// should not matter
			throw new ExecutionException("Wrapping a dependent exception in VDLFunction.post() - errors in data dependencies",e);
		}
	}

	protected void ret(VariableStack stack, final Object value) throws ExecutionException {
		if (value != null) {
			final VariableArguments vret = ArgUtil.getVariableReturn(stack);
			if (value.getClass().isArray()) {
				if (value.getClass().getComponentType().isPrimitive()) {
					vret.append(value);
				}
				else {
					Object[] array = (Object[]) value;
                    for (int i = 0; i < array.length; i++) {
                        vret.append(array[i]);
                    }
				}
			}
			else {
				vret.append(value);
			}
		}
	}

	protected abstract Object function(VariableStack stack) throws ExecutionException;

	/*
	 * This will likely break if the engine changes in fundamental ways. It also
	 * depends on the fact that iteration variable is named '$' in this
	 * particular implementation.
	 */
	public static String getThreadPrefix(VariableStack stack) throws ExecutionException {
		stack = stack.copy();
		ThreadingContext last = ThreadingContext.get(stack);
		Stack<Object> s = new Stack<Object>();
		while (stack.frameCount() > 1) {
			StackFrame frame = stack.currentFrame();
			if (frame.isDefined("$")) {
				List<?> itv = (List<?>) frame.getVar("$");
				s.push(itv.get(0));
				stack.leave();
				last = ThreadingContext.get(stack);
			}
			else {
				ThreadingContext tc = ThreadingContext.get(stack);
				if (!last.equals(tc)) {
					s.push(String.valueOf(last.getLastID()));
					last = tc;
				}
				stack.leave();
			}
		}

		StringBuffer sb = new StringBuffer();
		while (!s.isEmpty()) {
			sb.append(s.pop());
			if (!s.isEmpty()) {
				sb.append('-');
			}
		}
		return sb.toString();
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
			return new Double(TypeUtil.toDouble(value));
		}
		else if (Types.INT.equals(type)) {
			return new Double(TypeUtil.toInt(value));
		}
		else if (Types.BOOLEAN.equals(type)) {
			return new Boolean(TypeUtil.toBoolean(value));
		}
		else {
			return value;
		}
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static String[] filename(VariableStack stack) throws ExecutionException {
		DSHandle handle = (DSHandle)PA_VAR.getValue(stack);
		return filename(stack, handle);
	}
	
	public static String[] filename(VariableStack stack, DSHandle handle) throws ExecutionException {
        return filename(handle);
	}

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
	    Mapper mapper;
	    	    
        synchronized (var.getRoot()) {
            mapper = var.getMapper();
        }
        
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
	
	private static class PathComparator implements Comparator<Path> {
		public int compare(Path p1, Path p2) {
			for (int i = 0; i < Math.min(p1.size(), p2.size()); i++) {
				int d; 
				d = indexOrder(p1.isArrayIndex(i), p2.isArrayIndex(i));
				if (d != 0) {
					return d;
				}
				if (p1.isArrayIndex(i)) {
					d = numericOrder(p1.getElement(i), p2.getElement(i));
				}
				else {
					d = p1.getElement(i).compareTo(p2.getElement(i));
				}
				if (d != 0) {
					return d;
				}
			}
			//the longer one wins
			return p1.size() - p2.size();
		}
		
		private int indexOrder(boolean i1, boolean i2) {
			//it doesn't matter much what the order between indices and non-indices is,
			//but it needs to be consistent
			if (i1) {
				if (!i2) {
					return -1;
				}
			}
			else {
				if (i2) {
					return 1;
				}
			}
			return 0;
		}
		
		private int numericOrder(String i1, String i2) {
			//TODO check if we're actually dealing with numeric indices
			return Integer.parseInt(i1) - Integer.parseInt(i2);
		}
	}
	
	private static String leafFileName(DSHandle var) throws ExecutionException {
	    return leafFileName(var, var.getMapper());
	}

	private static String leafFileName(DSHandle var, Mapper mapper) throws ExecutionException {
		if (Types.STRING.equals(var.getType())) {
			return relativize(String.valueOf(var.getValue()));
		}
		else {
			if (var.getMapper() == null) {
				throw new ExecutionException("Cannot invoke filename() on data without a mapper: " + var);
			}
			PhysicalFormat f = var.getMapper().map(var.getPathFromRoot());
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
		return new AbsFile(file).getPath();
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
	public String argList(String[] s, boolean relative) {
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
		if (name != null && name.length() > 0 && name.charAt(0) == '/') {
			return name.substring(1);
		}
		else {
			return name;
		}
	}

	protected static Map getLogData(VariableStack stack) throws ExecutionException {
		try {
			return (Map) stack.getDeepVar(RestartLog.LOG_DATA);
		}
		catch (VariableNotFoundException e) {
			throw new ExecutionException("No log data found. Missing restartLog()?");
		}
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

	protected void closeChildren(VariableStack stack, AbstractDataNode handle) throws ExecutionException,
			InvalidPathException {
		// Close the future
		handle.closeShallow();
		// Mark all leaves
		for (DSHandle child : handle.getFields(Path.CHILDREN)) {
			child.closeShallow();
		}
	}
		
	public static AbstractDataNode[] waitForAllVargs(VariableStack stack) throws ExecutionException {
	    AbstractDataNode[] args = SwiftArg.VARGS.asDataNodeArray(stack);

        for (int i = 0; i < args.length; i++) {
            args[i].waitFor();
        }
        
        return args;
	}

	public static Path parsePath(Object o, VariableStack stack) throws ExecutionException {
		Path q = Path.EMPTY_PATH;
		Path p;
		if (o instanceof Path) {
			p = (Path) o;
		}
		else {
			p = Path.parse(TypeUtil.toString(o));
		}
		for (int i = 0; i < p.size(); i++) {
			if (p.isArrayIndex(i)) {
				if (p.isWildcard(i)) {
					q = q.addLast(p.getElement(i), true);
				}
				else {
					String index = p.getElement(i);
					try {
						// check this is can parse as an integer by trying to parse and getting an exception if not
						Integer.parseInt(index);
						q = q.addLast(index, true);
					}
					catch (NumberFormatException e) {
						Object v = stack.getVar(index);
						if (v instanceof DSHandle) {
							v = ((DSHandle) v).getValue();
						}
						q = q.addLast(TypeUtil.toString(v), true);
					}
				}
			}
			else {
				q = q.addLast(p.getElement(i));
			}
		}
		if (p.hasWildcards() && !q.hasWildcards()) {
			throw new RuntimeException("Error in the wildcard processing routine");
		}
		return q;
	}

	private static Set warnset = new HashSet();

	protected TCEntry getTCE(TCCache tc, FQN fqn, BoundContact bc) {
		List l;
		try {
			l = tc.getTCEntries(fqn, bc.getHost(), TCType.INSTALLED);
		}
		catch (Exception e) {
			throw new KarajanRuntimeException(e);
		}
		if (l == null || l.isEmpty()) {
			return null;
		}
		if (l.size() > 1) {
			synchronized (warnset) {
				LinkedList wl = new LinkedList();
				wl.add(fqn);
				wl.add(bc);
				if (!warnset.contains(wl)) {
					logger.warn("Multiple entries found for " + fqn + " on " + bc
							+ ". Using the first one");
					warnset.add(wl);
				}
			}
		}
		return (TCEntry) l.get(0);
	}

	public static final String TC = "vdl:TC";

	public static TCCache getTC(VariableStack stack) throws ExecutionException {
		synchronized (stack.firstFrame()) {
			TCCache tc = (TCCache) stack.firstFrame().getVar(TC);
			if (tc == null) {
				String prop = ConfigProperty.getProperty(VDL2ConfigProperties.TC_FILE, stack);
				Loader.debugText("TC", new java.io.File(prop));
				tc = new TCCache(File.getNonSingletonInstance(prop));
				stack.firstFrame().setVar(TC, tc);
			}
			return tc;
		}
	}

	private static int provenanceIDCount = 451000;

	public static synchronized int nextProvenanceID() {
		return provenanceIDCount++;
	}

	public static void logProvenanceResult(int id, DSHandle result, 
	        String name) 
	throws ExecutionException {
	    if (logger.isDebugEnabled())
	        logger.debug("FUNCTION id="+id+" name="+name+" result="+result.getIdentifier());
	    else if (logger.isInfoEnabled())
	        logger.info("FUNCTION: " + name + "()");
	}

	public static void logProvenanceParameter(int id, DSHandle parameter, String paramName) throws ExecutionException {
	    if (logger.isDebugEnabled())
	        logger.debug("FUNCTIONPARAMETER id="+id+" input="+parameter.getIdentifier()+" name="+paramName);
	}
}
