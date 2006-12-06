/*
 * Created on Jun 5, 2006
 */
package org.griphyn.vdl.karajan;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.TaskConstraints;
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
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.cog.karajan.workflow.nodes.restartLog.LogEntry;
import org.globus.cog.karajan.workflow.nodes.restartLog.MutableInteger;
import org.globus.cog.karajan.workflow.nodes.restartLog.RestartLog;
import org.griphyn.common.catalog.TransformationCatalog;
import org.griphyn.common.catalog.TransformationCatalogEntry;
import org.griphyn.common.catalog.transformation.TCMode;
import org.griphyn.common.classes.TCType;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.util.FQN;

public class VdlLib extends FunctionsCollection {
	public static final Arg OA_TYPE = new Arg.Optional("type", null);
	public static final Arg OA_MAPPING = new Arg.Optional("mapping", null);
	public static final Arg OA_VALUE = new Arg.Optional("value", null);
	public static final Arg OA_ISARRAY = new Arg.Optional("isArray", Boolean.FALSE);
	public static final Arg OA_DBGNAME = new Arg.Optional("dbgname", null);

	static {
		setArguments("vdl_new",
				new Arg[] { OA_TYPE, OA_MAPPING, OA_VALUE, OA_ISARRAY, OA_DBGNAME, });
	}

	public Object vdl_new(VariableStack stack) throws ExecutionException {
		String type = TypeUtil.toString(OA_TYPE.getValue(stack));
		Object value = OA_VALUE.getValue(stack);
		Map mapping = (Map) OA_MAPPING.getValue(stack);
		boolean isArray = TypeUtil.toBoolean(OA_ISARRAY.getValue(stack));
		String dbgname = TypeUtil.toString(OA_DBGNAME.getValue(stack));
		if (dbgname != null) {
			if (mapping == null) {
				mapping = new HashMap();
			}
			mapping.put("dbgname", dbgname);
		}
		if (type == null && value == null) {
			throw new ExecutionException("You must specify either a type or a value!");
		}
		if (mapping != null) {
			String mapper = (String) mapping.get("descriptor");
			if ("concurrent_mapper".equals(mapper)) {
				String threadPrefix = getThreadPrefix(stack);
				System.err.println("Thread: " + ThreadingContext.get(stack) + ", ThreadPrefix: "
						+ threadPrefix);
				mapping.put(ConcurrentMapper.PARAM_THREAD_PREFIX, threadPrefix);
			}
		}
		try {
			AbstractDataNode handle;
			if (isArray) {
				// dealing with array variable
				handle = new RootArrayDataNode(type);
				if (value != null) {
					if (value instanceof RootArrayDataNode) {
						handle = (RootArrayDataNode) value;
					}
					else {
						if (!(value instanceof List)) {
							throw new ExecutionException(
									"An array variable can only be initialized with a list of values!");
						}
						int index = 0;
						Iterator i = ((List) value).iterator();
						while (i.hasNext()) {
							Object n = i.next();
							Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
							// TODO check consistency of the list: primitive or
							// dataset
							if (n instanceof DSHandle) {
								handle.getField(p).set((DSHandle) n);
							}
							else {
								DSHandle field = handle.getField(p);
								field.setValue(n);

								closeShallow(stack, field);
							}
							index++;
						}
					}
					closeShallow(stack, handle);
				}

				if (mapping != null) {
					handle.init(mapping);
				}

				return handle;
			}
			if (value instanceof DSHandle) {
				return value;
			}
			else if (type != null) {
				handle = new RootDataNode(type);
				if (mapping != null) {
					handle.init(mapping);
				}
				if (value != null) {
					handle.setValue(internalValue(value));
					closeShallow(stack, handle);
				}
			}
			else {
				handle = new RootDataNode(inferType(value));
				handle.setValue(internalValue(value));
				closeShallow(stack, handle);
			}
			return handle;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ExecutionException(e);
		}
	}
	
	public Object vdl_threadprefix(VariableStack stack) throws ExecutionException {
		return getThreadPrefix(stack);
	}

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

	private String inferType(Object value) {
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

	private Object internalValue(Object value) {
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

	private boolean compatible(String expectedType, String actualType) {
		if (expectedType.equals("float")) {
			if (actualType.equals("float") || actualType.equals("int"))
				return true;
			else
				return false;
		}
		return actualType.equals(expectedType);
	}

	public static final Arg OA_PATH = new Arg.Optional("path", "");
	public static final Arg PA_PATH = new Arg.Positional("path");
	public static final Arg PA_VAR = new Arg.TypedPositional("var", DSHandle.class, "handle");

	static {
		setArguments("vdl_getfield", new Arg[] { OA_PATH, PA_VAR });
	}

	public Object vdl_getfield(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle field = var.getField(path);
			return field;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	public static final Arg PA_VAR1 = new Arg.Positional("var");
	// public static final Arg OA_PATH = new Arg.Optional("path", "");

	static {
		setArguments("vdl_getfieldvalue", new Arg[] { PA_VAR1, OA_PATH });
	}

	public Object vdl_getfieldvalue(VariableStack stack) throws ExecutionException {
		Object var1 = PA_VAR1.getValue(stack);
		if (!(var1 instanceof DSHandle))
			return var1;
		DSHandle var = (DSHandle) var1;
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			if (path.hasWildcards()) {
				try {
					return var.getFields(path).toArray();
				}
				catch (HandleOpenException e) {
					throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
				}
			}
			else {
				var = var.getField(path);
				if (var.isArray()) {
					throw new ExecutionException("getfieldvalue called on an array: " + var);
				}
				synchronized (var) {
					Object value = var.getValue();
					if (value == null) {
						throw new FutureNotYetAvailable(addFutureListener(stack, var));
					}
					else {
						return value;
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}
	static {
		setArguments("vdl_getarrayfieldvalue", new Arg[] { PA_VAR, OA_PATH });
	}

	public Object vdl_getarrayfieldvalue(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			synchronized (var) {
				var = var.getField(path);
				Map value = var.getArrayValue();
				if (var.isClosed()) {
					// System.err.println("gafv: " + var + "." + path + " ->
					// OKIT[]");
					return new PairIterator(value);
				}
				else {
					return addFutureListListener(stack, var, value);
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments("vdl_setfieldvalue", new Arg[] { OA_PATH, PA_VAR, PA_VALUE });
	}

	public void vdl_setfieldvalue(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			Path path = parsePath(OA_PATH.getValue(stack), stack);
			DSHandle leaf = var.getField(path);
			synchronized (leaf) {
				leaf.setValue(PA_VALUE.getValue(stack));
				closeShallow(stack, leaf);
			}
		}
		catch (Exception e) {
			throw new ExecutionException(e.getMessage() + " for variable " + var, e);
		}
	}

	public Object vdl_mapping(VariableStack stack) throws ExecutionException {
		return null;
	}

	static {
		setArguments("vdl_isdatasetbound", new Arg[] { PA_VAR });
	}

	public boolean vdl_isdatasetbound(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		if (var instanceof AbstractDataNode) {
			return !((AbstractDataNode) var).isPrimitive();
		}
		else {
			return false;
		}
	}

	static {
		setArguments("vdl_filename", new Arg[] { PA_PATH, PA_VAR });
	}

	public Object vdl_filename(VariableStack stack) throws ExecutionException {
		return argList(filename(stack), true);
	}

	static {
		setArguments("vdl_filenames", new Arg[] { PA_PATH, PA_VAR });
	}

	public Object vdl_filenames(VariableStack stack) throws ExecutionException {
		String[] f = filename(stack);
		for (int i = 0; i < f.length; i++) {
			f[i] = relativize(f[i]);
		}
		return f;
	}

	static {
		setArguments("vdl_absfilename", new Arg[] { PA_PATH, PA_VAR });
	}

	public Object vdl_absfilename(VariableStack stack) throws ExecutionException {
		return filename(stack);
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

	static {
		setArguments("vdl_fileset", new Arg[] { PA_VAR, OA_PATH });
	}

	public Collection vdl_fileset(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			return var.getField(parsePath(OA_PATH.getValue(stack), stack)).getFileSet();
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	static {
		setArguments("vdl_fringepaths", new Arg[] { PA_VAR, OA_PATH });
	}

	public Collection vdl_fringepaths(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			var = var.getField(parsePath(OA_PATH.getValue(stack), stack));
			Collection c = var.getFringePaths();
			return c;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		catch (HandleOpenException e) {
			throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
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

	static {
		setArguments("vdl_closedataset", new Arg[] { PA_VAR, OA_PATH });
	}

	// TODO path is not used!
	public void vdl_closedataset(VariableStack stack) throws ExecutionException {
		Path path = parsePath(OA_PATH.getValue(stack), stack);
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		try {
			var = var.getField(path);
			closeChildren(stack, var);
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	public static final Arg A_IN = new Arg.Positional("in");
	public static final Arg A_OUT = new Arg.Positional("out");

	static {
		setArguments("vdl_importsitecatalog", new Arg[] { A_IN, A_OUT });
	}

	public void vdl_importsitecatalog(VariableStack stack) throws ExecutionException {
		String in = TypeUtil.toString(A_IN.getValue(stack));
		String out = TypeUtil.toString(A_OUT.getValue(stack));

		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			URL trres = getClass().getClassLoader().getResource("sites2resources.xsl");
			if (trres == null) {
				throw new ExecutionException(
						"Site transformer (sites2resources.xsl) was not found on the class path");
			}
			Transformer transformer = tFactory.newTransformer(new StreamSource(trres.openStream()));
			URL sites = getClass().getClassLoader().getResource(in);
			if (sites == null) {
				throw new ExecutionException("Site catalog (" + in
						+ ") was not found on the class path");
			}
			transformer.transform(new StreamSource(sites.openStream()), new StreamResult(
					new FileOutputStream(out)));
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}

	}

	public static final Arg A_TR = new Arg.Positional("tr");

	static {
		setArguments("vdl_getjobconstraints", new Arg[] { A_TR });
	}

	public Object vdl_getjobconstraints(VariableStack stack) throws ExecutionException {
		String tr = TypeUtil.toString(A_TR.getValue(stack));
		TaskConstraints tc = new TaskConstraints();
		tc.addConstraint("tr", tr);
		tc.addConstraint("trfqn", new FQN(tr));
		return tc;
	}

	public static final Arg A_DVAR = new Arg.TypedPositional("dvar", DSHandle.class, "handle");
	public static final Arg A_SVAR = new Arg.Positional("svar");
	public static final Arg A_DPATH = new Arg.Optional("dpath", "");
	public static final Arg A_SPATH = new Arg.Optional("spath", "");

	static {
		setArguments("vdl_assign", new Arg[] { A_DVAR, A_SVAR, A_DPATH, A_SPATH });
	}

	public void vdl_assign(VariableStack stack) throws ExecutionException {
		Path dpath = parsePath(A_DPATH.getValue(stack), stack);
		Path spath = parsePath(A_SPATH.getValue(stack), stack);
		DSHandle dvar = (DSHandle) A_DVAR.getValue(stack);
		Object s = A_SVAR.getValue(stack);
		if (s == null) {
			throw new ExecutionException("Source variable is null");
		}
		try {
			dvar = dvar.getField(dpath);
			if (s instanceof List) {
				if (!Path.EMPTY_PATH.equals(spath)) {
					throw new ExecutionException(
							"If the source is an array there can be no source path (" + spath + ")");
				}
				int index = 0;
				Iterator i = ((List) s).iterator();
				while (i.hasNext()) {
					Object n = i.next();
					Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					if (n instanceof DSHandle) {
						dvar.getField(p).set((DSHandle) n);
					}
					else {
						DSHandle field = dvar.getField(p);
						field.setValue(n);
						closeShallow(stack, field);
					}
					index++;
				}
				closeShallow(stack, dvar);
			}
			else if (s instanceof DSHandle) {
				DSHandle svar = (DSHandle) s;
				svar = svar.getField(spath);
				/*
				 * Iterator i = svar.getFringePaths().iterator(); while
				 * (i.hasNext()) { String leafSPath = (String) i.next(); Path
				 * leafPath = Path.parse(leafSPath); mergeListeners(stack,
				 * dvar.getField(leafPath), svar.getField(leafPath)); }
				 */
				dvar.set(svar);
			}
			else {
				dvar.setValue(s);
				closeShallow(stack, dvar);
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
	}

	static {
		setArguments("vdl_islogged", new Arg[] { PA_VAR, PA_PATH });
	}

	public boolean vdl_islogged(VariableStack stack) throws ExecutionException {
		String fileName = getFileName(stack);
		Map map = getLogData(stack);
		LogEntry entry = LogEntry.build(fileName);
		boolean found = false;
		synchronized (map) {
			MutableInteger count = (MutableInteger) map.get(entry);
			if (count != null && count.getValue() > 0) {
				count.dec();
				found = true;
			}
		}
		return found;
	}

	private Map getLogData(VariableStack stack) throws ExecutionException {
		try {
			return (Map) stack.getDeepVar(RestartLog.LOG_DATA);
		}
		catch (VariableNotFoundException e) {
			throw new ExecutionException("No log data found. Missing restartLog()?");
		}
	}

	static {
		setArguments("vdl_logvar", new Arg[] { PA_VAR, PA_PATH });
	}

	public void vdl_logvar(VariableStack stack) throws ExecutionException {
		RestartLog.LOG_CHANNEL.ret(stack, getFileName(stack));
	}

	private String getFileName(VariableStack stack) throws ExecutionException {
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

	public static final Arg PA_FROM = new Arg.Positional("from");
	public static final Arg PA_TO = new Arg.Positional("to");
	public static final Arg OA_STEP = new Arg.Optional("step");

	static {
		setArguments("vdl_range", new Arg[] { PA_FROM, PA_TO, OA_STEP });
	}

	public Object vdl_range(VariableStack stack) throws ExecutionException {
		// TODO: deal with expression
		Object from = PA_FROM.getValue(stack);
		Object to = PA_TO.getValue(stack);
		Object step = OA_STEP.getValue(stack);

		String type = "";
		Object start, stop, incr;
		if (from instanceof DSHandle) {
			type = ((DSHandle) from).getType();
			start = ((DSHandle) from).getValue();
		}
		else {
			type = inferType(from);
			start = from;
		}

		if (to instanceof DSHandle) {
			if (!compatible(((DSHandle) to).getType(), type))
				throw new ExecutionException("Range: from and to type mismatch");
			stop = ((DSHandle) to).getValue();
		}
		else {
			if (!compatible(inferType(to), type))
				throw new ExecutionException("Range: from and to type mismatch");
			stop = to;
		}

		if (step != null) {
			if (step instanceof DSHandle) {
				if (!compatible(((DSHandle) step).getType(), type))
					throw new ExecutionException("Range: step type mismatch");
				incr = ((DSHandle) step).getValue();
			}
			else {
				if (!compatible(inferType(step), type))
					throw new ExecutionException("Range: step type mismatch");
				incr = step;
			}
		}
		else {
			incr = new Integer(1);
		}
		// only deal with int and float
		try {
			AbstractDataNode handle;
			if (type.equals("int")) {
				handle = new RootArrayDataNode(type);
				int s = Integer.parseInt(start.toString());
				int t = Integer.parseInt(stop.toString());
				int p = Integer.parseInt(incr.toString());
				int index = 0;
				for (int v = s; v <= t; v += p, index++) {
					Path path = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					DSHandle field = handle.getField(path);
					field.setValue(new Integer(v));
					closeShallow(stack, field);
				}
			}
			else if (type.equals("float")) {
				handle = new RootArrayDataNode(type);
				double s = Double.parseDouble(start.toString());
				double t = Double.parseDouble(stop.toString());
				double p = Double.parseDouble(incr.toString());
				int index = 0;
				for (double v = s; v <= t; v += p, index++) {
					Path path = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					DSHandle field = handle.getField(path);
					field.setValue(new Double(v));
					closeShallow(stack, field);
				}
			}
			else
				return null;
			closeShallow(stack, handle);
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}

	}

	public static final Arg PA_TYPE = new Arg.Positional("type");
	public static final Arg OA_ARGNAME = new Arg.Optional("argname");

	static {
		setArguments("vdl_typecheck", new Arg[] { PA_VAR, PA_TYPE, OA_ISARRAY, OA_ARGNAME });
	}

	public void vdl_typecheck(VariableStack stack) throws ExecutionException {
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		String type = TypeUtil.toString(PA_TYPE.getValue(stack));
		boolean isArray = TypeUtil.toBoolean(OA_ISARRAY.getValue(stack));
		String argname = TypeUtil.toString(OA_ARGNAME.getValue(stack, null));
		if (!compatible(type, var.getType()) || !(isArray == var.isArray())) {
			if (argname != null) {
				throw new ExecutionException("Wrong type for argument '" + argname + "'. Expected "
						+ type + (isArray ? "[]" : "") + "; got " + var.getType()
						+ (var.isArray() ? "[]" : "") + ". Actual argument: " + var);
			}
			else {
				throw new ExecutionException("Wrong type for argument. Expected " + type
						+ (isArray ? "[]" : "") + "; got " + var.getType()
						+ (var.isArray() ? "[]" : "") + ". Actual argument: " + var);
			}
		}
	}

	private void closeChildren(VariableStack stack, DSHandle handle) throws ExecutionException,
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

	private void closeShallow(VariableStack stack, DSHandle handle) throws ExecutionException {
		handle.closeShallow();
		getFutureWrapperMap(stack).close(handle);
	}

	private boolean isClosed(VariableStack stack, DSHandle handle) throws ExecutionException {
		return getFutureWrapperMap(stack).isClosed(handle);
	}

	private Future addFutureListener(VariableStack stack, DSHandle handle)
			throws ExecutionException {
		return getFutureWrapperMap(stack).addNodeListener(handle);
	}

	private FutureIterator addFutureListListener(VariableStack stack, DSHandle handle, Map value)
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
	
	public static final Arg PA_TR = new Arg.Positional("tr");
	public static final Arg PA_HOST = new Arg.Positional("host");
	
	static {
		setArguments("vdl_executable", new Arg[] { PA_TR, PA_HOST });
	}
	
	private static TransformationCatalog tc;
	private static Set warnset = new HashSet();
	
	public String vdl_executable(VariableStack stack) throws ExecutionException {
		synchronized(VdlLib.class) {
			if (tc == null) {
				tc = TCMode.loadInstance();
			}
		}
		String tr = TypeUtil.toString(PA_TR.getValue(stack));
		BoundContact bc = (BoundContact) PA_HOST.getValue(stack);
		FQN fqn = new FQN(tr);
		List l;
		try {
			l = tc.getTCEntries(fqn.getNamespace(), fqn.getName(), fqn.getVersion(),
					bc.getHost(), TCType.INSTALLED);
			}
			catch (Exception e) {
				throw new KarajanRuntimeException(e);
			}
			if (l == null || l.isEmpty()) {
				return tr;
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

			TransformationCatalogEntry tce = (TransformationCatalogEntry) l.get(0);
			return tce.getPhysicalTransformation();
	}
}
