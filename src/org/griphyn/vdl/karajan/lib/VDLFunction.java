/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
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
import org.griphyn.vdl.karajan.WrapperMap;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.util.FQN;
import org.griphyn.vdl.util.VDL2ConfigProperties;

public abstract class VDLFunction extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(VDLFunction.class);

	public static final Arg.Channel ERRORS = new Arg.Channel("errors");

	public static final Arg OA_PATH = new Arg.Optional("path", "");
	public static final Arg PA_PATH = new Arg.Positional("path");
	public static final Arg PA_VAR = new Arg.TypedPositional("var", DSHandle.class, "handle");
	public static final Arg OA_ISARRAY = new Arg.Optional("isArray", Boolean.FALSE);

	public final void post(VariableStack stack) throws ExecutionException {
		try {
			Object o = function(stack);
			if (o != null) {
				ret(stack, o);
			}
			super.post(stack);
		}
		catch (DependentException e) {
			// This would not be the primal fault so in non-lazy errors mode it
			// should not matter
			ERRORS.ret(stack, e.getMessage());
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

	protected abstract Object function(VariableStack stack) throws ExecutionException;

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

	protected String inferType(Object value) {
		// TODO
		if (value instanceof String) {
			String str = (String) value;
			if (str.startsWith("\"")) {
				return "string";
			}
			try {
				Integer.parseInt(str);
				return "int";
			}
			catch (NumberFormatException e) {
			}
			try {
				Double.parseDouble(str);
				return "float";
			}
			catch (NumberFormatException e) {
			}

			if (value.equals("true") || value.equals("false"))
				return "boolean";

			throw new RuntimeException("Invalid value: " + value);
		}
		else if (value instanceof Integer) {
			return "int";
		}
		else if (value instanceof Double) {
			return "float";
		}
		else if (value instanceof Boolean) {
			return "boolean";
		}
		else {
			return "string";
		}
	}

	protected Object internalValue(Object value) {
		if (value instanceof String) {
			String strval = (String) value;
			if (strval.startsWith("\"") && strval.endsWith("\"")) {
				return strval.substring(1, strval.length() - 1);
			}
			else {
				return strval;
			}
		}
		else {
			return value;
		}
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public String[] filename(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path;
			if (PA_PATH.isPresent(stack)) {
				path = parsePath(PA_PATH.getValue(stack), stack);
			}
			else {
				path = Path.EMPTY_PATH;
			}
			if (path.hasWildcards()) {
				try {
					List l = new ArrayList();
					Iterator i = var.getFields(path).iterator();
					while (i.hasNext()) {
						DSHandle handle = (DSHandle) i.next();
						l.add(handle.getFilename());
					}
					return (String[]) l.toArray(EMPTY_STRING_ARRAY);
				}
				catch (HandleOpenException e) {
					throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
				}
			}
			else {
				var = var.getField(path);
				if (var.isArray()) {
					List l = var.getFileSet();
					return (String[]) l.toArray(EMPTY_STRING_ARRAY);
				}
				else {
					String filename = var.getFilename();
					if (filename == null) {
						throw new ExecutionException("Mapper did not provide a file name for "
								+ path);
					}
					else {
						return new String[] { filename };
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

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

	public String relativize(String name) {
		if (name != null && name.length() > 0 && name.charAt(0) == '/') {
			return name.substring(1);
		}
		else {
			return name;
		}
	}

	public static final String VDL_FUTURE_WRAPPER_MAP = "#vdl:futureWrapperMap";

	private WrapperMap getFutureWrapperMap(VariableStack stack) throws ExecutionException {
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

	protected String getFileName(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(PA_PATH.getValue(stack), stack);
			String filename = var.getField(path).getFilename();
			if (filename == null) {
				throw new ExecutionException("Mapper did not provide a file name for " + path);
			}
			else {
				return filename;
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	protected boolean compatible(String expectedType, String actualType) {
		if (expectedType.equals("float")) {
			if (actualType.equals("float") || actualType.equals("int"))
				return true;
			else
				return false;
		}
		return actualType.equals(expectedType);
	}

	protected void closeChildren(VariableStack stack, DSHandle handle) throws ExecutionException,
			InvalidPathException {
		WrapperMap hash = getFutureWrapperMap(stack);
		// Close the future
		handle.closeShallow();
		hash.close(handle);
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
			throw new ExecutionException(e);
		}

		markToRoot(stack, handle);
	}

	private void markToRoot(VariableStack stack, DSHandle handle) throws ExecutionException {
		// Also mark all arrays from root
		Path fullPath = handle.getPathFromRoot();
		DSHandle root = handle.getRoot();
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

	private void closeDeep(VariableStack stack, DSHandle handle) throws ExecutionException,
			InvalidPathException {
		// Close the future
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
			throw new ExecutionException(e);
		}
		markToRoot(stack, handle);
	}

	protected void closeShallow(VariableStack stack, DSHandle handle) throws ExecutionException {
		handle.closeShallow();
		getFutureWrapperMap(stack).close(handle);
	}

	private boolean isClosed(VariableStack stack, DSHandle handle) throws ExecutionException {
		return getFutureWrapperMap(stack).isClosed(handle);
	}

	protected Future addFutureListener(VariableStack stack, DSHandle handle)
			throws ExecutionException {
		return getFutureWrapperMap(stack).addNodeListener(handle);
	}

	protected FutureIterator addFutureListListener(VariableStack stack, DSHandle handle, Map value)
			throws ExecutionException {
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
		Path p = Path.parse(TypeUtil.toString(o));
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
