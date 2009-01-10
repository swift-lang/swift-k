package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
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
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;
import org.griphyn.common.catalog.TransformationCatalogEntry;
import org.griphyn.common.catalog.transformation.File;
import org.griphyn.common.classes.TCType;
import org.griphyn.vdl.karajan.InHook;
import org.griphyn.vdl.karajan.Monitor;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.karajan.VDL2FutureException;
import org.griphyn.vdl.karajan.WrapperMap;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.GeneralizedFileFormat;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
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
		catch (HandleOpenException e) {
			throw new FutureNotYetAvailable(VDLFunction.addFutureListener(stack, e.getSource()));
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
				try {
					Object[] array = (Object[]) value;
					for (int i = 0; i < array.length; i++) {
						vret.append(array[i]);
					}
				}
				catch (ClassCastException e) {
					// array of primitives; return as is
					vret.append(value);
				}
			}
			else {
				vret.append(value);
			}
		}
	}

	protected abstract Object function(VariableStack stack) throws ExecutionException,
			HandleOpenException;

	/*
	 * This will likely break if the engine changes in fundamental ways. It also
	 * depends on the fact that iteration variable is named '$' in this
	 * particular implementation.
	 */
	public static String getThreadPrefix(VariableStack stack) throws ExecutionException {
		stack = stack.copy();
		ThreadingContext last = ThreadingContext.get(stack);
		Stack s = new Stack();
		while (stack.frameCount() > 1) {
			StackFrame frame = stack.currentFrame();
			if (frame.isDefined("$")) {
				List itv = (List) frame.getVar("$");
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
		else if (value instanceof String) {
			return (String) value;
		}
		else {
			return value;
		}
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public String[] filename(VariableStack stack) throws ExecutionException {
		DSHandle ovar = (DSHandle)PA_VAR.getValue(stack);
		synchronized(ovar.getRoot()) {
			try {
				return filename(ovar);
			}
                	catch(VDL2FutureException ve) {
				throw new FutureNotYetAvailable(addFutureListener(stack, ve.getHandle()));
                	}
			catch (HandleOpenException e) {
				throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
			}
		}
	} 

	/** The caller is expected to have synchronized on the root of var. */
	public String[] filename(DSHandle var) throws ExecutionException, HandleOpenException {
		assert Thread.holdsLock(var.getRoot());
		try {
			if (var.getType().isArray()) {
				return leavesFileNames(var);
			}
			else {
				return new String[] { leafFileName(var) };
			}
		}
		catch (DependentException e) {
			return new String[0];
		}
	}

	private String[] leavesFileNames(DSHandle var) throws ExecutionException, HandleOpenException {
		List l = new ArrayList();
		Iterator i;
		try {
			Collection fp = var.getFringePaths();
			List src;
			if (fp instanceof List) {
				src = (List) fp;
			}
			else {
				src = new ArrayList(fp);
			}
			Collections.sort(src, new PathComparator());
			i = src.iterator();
			while (i.hasNext()) {
				Path p = (Path) i.next();
				l.add(leafFileName(var.getField(p)));
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException("DSHandle is lying about its fringe paths");
		}
		return (String[]) l.toArray(EMPTY_STRING_ARRAY);
	}
	
	private static class PathComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			Path p1 = (Path) o1;
			Path p2 = (Path) o2;
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

	private String leafFileName(DSHandle var) throws ExecutionException {
		if (Types.STRING.equals(var.getType())) {
			return relativize(String.valueOf(var.getValue()));
		}
		else {
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

	protected String pathOnly(String file) {
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
	public String relativize(String name) {
		name = pathOnly(name);
		if (name != null && name.length() > 0 && name.charAt(0) == '/') {
			return name.substring(1);
		}
		else {
			return name;
		}
	}

	public static final String VDL_FUTURE_WRAPPER_MAP = "#vdl:futureWrapperMap";

	protected static WrapperMap getFutureWrapperMap(VariableStack stack) throws ExecutionException {
		synchronized (stack.getExecutionContext()) {
			WrapperMap hash = (WrapperMap) stack.firstFrame().getVar(VDL_FUTURE_WRAPPER_MAP);
			if (hash == null) {
				hash = new WrapperMap();
				stack.firstFrame().setVar(VDL_FUTURE_WRAPPER_MAP, hash);
				InHook.install(new Monitor(hash));
			}
			return hash;
		}
	}

	protected Map getLogData(VariableStack stack) throws ExecutionException {
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

	protected void closeChildren(VariableStack stack, DSHandle handle) throws ExecutionException,
			InvalidPathException {
		WrapperMap hash = getFutureWrapperMap(stack);
		// Close the future
		boolean closed;
		synchronized(handle.getRoot()) {
			closed = handle.isClosed();
			if (!closed) {
				handle.closeShallow();
				hash.close(handle);
			}
		}
		try {
			// Mark all leaves
			Iterator it = handle.getFields(Path.CHILDREN).iterator();
			while (it.hasNext()) {
				DSHandle child = (DSHandle) it.next();
				child.closeShallow();
				hash.close(child);
			}
		}
		catch (HandleOpenException e) {
			throw new ExecutionException("Handle open in closeChildren",e);
		}
		
		if (!closed) {
			markToRoot(stack, handle);
		}
	}

	private void markToRoot(VariableStack stack, DSHandle handle) throws ExecutionException {
		// Also mark all arrays from root
		Path fullPath = handle.getPathFromRoot();
		DSHandle root = handle.getRoot();
		synchronized(root) {
			for (int i = 0; i < fullPath.size(); i++) {
				if (fullPath.isArrayIndex(i)) {
					try {
						markAsAvailable(stack, root.getField(fullPath.subPath(0, i)),
								fullPath.getElement(i));
					}
					catch (InvalidPathException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected void waitFor(VariableStack stack, DSHandle handle) throws ExecutionException {
		synchronized(handle.getRoot()) {
			if (!handle.isClosed()) {
				throw new FutureNotYetAvailable(addFutureListener(stack, handle));
			}
		}
	}

	private void closeDeep(VariableStack stack, DSHandle handle) throws ExecutionException,
			InvalidPathException {
		// Close the future
		synchronized(handle.getRoot()) {
		handle.closeShallow();
		getFutureWrapperMap(stack).close(handle);
		try {
			// Mark all nodes
			Iterator it = handle.getFields(Path.CHILDREN).iterator();
			while (it.hasNext()) {
				DSHandle child = (DSHandle) it.next();
				closeDeep(stack, child);
			}
		}
		catch (HandleOpenException e) {
			throw new ExecutionException("HandleOpen during closeDeep",e);
		}
		markToRoot(stack, handle);
		}
	}

	protected void closeShallow(VariableStack stack, DSHandle handle) throws ExecutionException {
		synchronized(handle.getRoot()) {
			handle.closeShallow();
			getFutureWrapperMap(stack).close(handle);
		}
	}

	private boolean isClosed(VariableStack stack, DSHandle handle) throws ExecutionException {
		assert Thread.holdsLock(handle.getRoot());
		return getFutureWrapperMap(stack).isClosed(handle);
	}

	protected static Future addFutureListener(VariableStack stack, DSHandle handle)
			throws ExecutionException {
		assert Thread.holdsLock(handle.getRoot());
		return getFutureWrapperMap(stack).addNodeListener(handle);
	}

	protected static FutureIterator addFutureListListener(VariableStack stack, DSHandle handle,
			Map value) throws ExecutionException {
		assert Thread.holdsLock(handle.getRoot());
		return getFutureWrapperMap(stack).addFutureListListener(handle, value).futureIterator(stack);
	}

	private void mergeListeners(VariableStack stack, DSHandle destination, DSHandle source)
			throws ExecutionException {
		getFutureWrapperMap(stack).mergeListeners(destination, source);
	}

	private void markAsAvailable(VariableStack stack, DSHandle handle, Object key)
			throws ExecutionException {
		getFutureWrapperMap(stack).markAsAvailable(handle, key);
	}

	protected final Path parsePath(Object o, VariableStack stack) throws ExecutionException {
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
						int ii = Integer.parseInt(index);
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

	protected TransformationCatalogEntry getTCE(TCCache tc, FQN fqn, BoundContact bc) {
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
		return (TransformationCatalogEntry) l.get(0);
	}

	public static final String TC = "vdl:TC";

	public static TCCache getTC(VariableStack stack) throws ExecutionException {
		synchronized (stack.firstFrame()) {
			TCCache tc = (TCCache) stack.firstFrame().getVar(TC);
			if (tc == null) {
				String prop = ConfigProperty.getProperty(VDL2ConfigProperties.TC_FILE, stack);
				tc = new TCCache(File.getNonSingletonInstance(prop));
				stack.firstFrame().setVar(TC, tc);
			}
			return tc;
		}
	}
}
