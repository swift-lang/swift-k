// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.Property;
import org.globus.cog.karajan.util.RangeIterator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class Misc extends FunctionsCollection {

	private static final Logger logger = Logger.getLogger(FunctionsCollection.class);

	private static long UIDIndex = System.currentTimeMillis();

	public static final Arg PA_FILE = new Arg.Positional("file");
	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments("sys_contains", new Arg[] { PA_FILE, PA_VALUE });
		addAlias("sys_file_contains", "sys_contains");
	}

	public boolean sys_contains(VariableStack stack) throws ExecutionException {
		File f = TypeUtil.toFile(stack, PA_FILE);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			String expanded = TypeUtil.toString(PA_VALUE.getValue(stack));
			do {
				line = br.readLine();
				if (line.indexOf(expanded) != -1) {
					return true;
				}
			} while (line != null);
		}
		catch (Exception e) {
			throw new ExecutionException("Error reading file " + f, e);
		}
		return false;
	}

	static {
		setArguments("sys_equals", ARGS_2VALUES);
		addAlias("sys___eq___eq_", "sys_equals");
	}

	public boolean sys_equals(VariableStack stack) throws ExecutionException {
		Object[] args = getArguments(ARGS_2VALUES, stack);
		if (args[0] instanceof Number) {
			try {
				Number n2 = TypeUtil.toNumber(args[1]);
				return ((Number) args[0]).doubleValue() == n2.doubleValue();
			}
			catch (Exception e) {
				return false;
			}
		}
		else {
			return args[0].equals(args[1]);
		}
	}

	static {
		setArguments("sys___bang___eq_", ARGS_2VALUES);
	}

	public boolean sys___bang___eq_(VariableStack stack) throws ExecutionException {
		Object[] args = getArguments(ARGS_2VALUES, stack);
		return !args[0].equals(args[1]);
	}

	public static final Arg PA_PATTERN = new Arg.Positional("pattern");

	static {
		setArguments("sys_numberformat", new Arg[] { PA_PATTERN, PA_VALUE });
	}

	public Object sys_numberformat(VariableStack stack) throws ExecutionException {
		DecimalFormat df = new DecimalFormat(TypeUtil.toString(PA_PATTERN.getValue(stack)));
		return df.format(TypeUtil.toDouble(PA_VALUE.getValue(stack)));
	}
	
	static {
        setArguments("sys_dateformat", new Arg[] { PA_PATTERN, PA_VALUE });
    }

    public Object sys_dateformat(VariableStack stack) throws ExecutionException {
        DateFormat df = new SimpleDateFormat(TypeUtil.toString(PA_PATTERN.getValue(stack)));
        return df.format(PA_VALUE.getValue(stack));
    }

	static {
		setArguments("sys_readfile", new Arg[] { PA_FILE });
		addAlias("sys_file_read", "sys_readfile");
	}

	public Object sys_readfile(VariableStack stack) throws ExecutionException {
		try {
			File f = TypeUtil.toFile(stack, PA_FILE);
			BufferedReader br = new BufferedReader(new FileReader(f));
			StringBuffer text = new StringBuffer();
			String line = br.readLine();
			while (line != null) {
				text.append(line);
				text.append('\n');
				line = br.readLine();
			}
			return text.toString();
		}
		catch (Exception e) {
			fail(stack, e.getMessage());
			return null;
		}
	}

	public static final Arg PA_OFFSET = new Arg.Positional("offset");

	static {
		setArguments("sys_file_readline", new Arg[] { PA_FILE, PA_OFFSET });
	}

	public Object sys_file_readline(VariableStack stack) throws ExecutionException {
		File f = TypeUtil.toFile(stack, PA_FILE);
		try {
			RandomAccessFile file = new RandomAccessFile(f, "r");
			try {
				file.seek(TypeUtil.toNumber(PA_OFFSET.getValue(stack)).longValue());
				return file.readLine();
			}
			finally {
				file.close();
			}
		}
		catch (Exception e) {
			fail(stack, e.getMessage());
			return null;
		}
	}

	public static final Arg OA_CONTEXT = new Arg.Optional("context");
	public static final Arg OA_PREFIX = new Arg.Optional("prefix", "");
	public static final Arg OA_SUFFIX = new Arg.Optional("suffix", "");

	static {
		setArguments("sys_uid", new Arg[] { OA_CONTEXT, OA_PREFIX, OA_SUFFIX });
	}

	public Object sys_uid(VariableStack stack) throws ExecutionException {
		String prefix = TypeUtil.toString(OA_PREFIX.getValue(stack));
		String suffix = TypeUtil.toString(OA_SUFFIX.getValue(stack));
		if (OA_CONTEXT.isPresent(stack)) {
			prefix = TypeUtil.toString(OA_CONTEXT.getValue(stack));
		}
		synchronized (Misc.class) {
			return prefix + alphanum(UIDIndex++) + suffix;
		}
	}

	public static final String codes = "0123456789abcdefghijklmnopqrstuvxyz";

	protected String alphanum(long val) {
		StringBuffer sb = new StringBuffer();
		int base = codes.length();
		while (val > 0) {
			int c = (int) (val % base);
			sb.append(codes.charAt(c));
			val = val / base;
		}
		return sb.toString();
	}

	public static final Arg PA_NAME = new Arg.Positional("name");
	public static final Arg.Channel CA_PROPERTIES = new Arg.Channel("properties");

	static {
		setArguments("sys_property", new Arg[] { PA_NAME, PA_VALUE });
	}

	public void sys_property(VariableStack stack) throws ExecutionException {
		Property prop = new Property();
		prop.setName(TypeUtil.toString(PA_NAME.getValue(stack)));
		prop.setValue(TypeUtil.toString(PA_VALUE.getValue(stack)));
		CA_PROPERTIES.ret(stack, prop);
	}

	static {
		setArguments("sys_number", new Arg[] { PA_VALUE });
	}

	public double sys_number(VariableStack stack) throws ExecutionException {
		return TypeUtil.toDouble(PA_VALUE.getValue(stack));
	}

	public static final Arg PA_TYPE = new Arg.Positional("type");

	static {
		setArguments("sys_outputstream", new Arg[] { PA_TYPE, PA_FILE });
	}

	public Object sys_outputstream(VariableStack stack) throws ExecutionException {
		String type = TypeUtil.toString(PA_TYPE.getValue(stack, null));
		if ("file".equalsIgnoreCase(type) || PA_FILE.isPresent(stack)) {
			File f = TypeUtil.toFile(stack, PA_FILE);
			try {
				return new PrintStream(new FileOutputStream(f));
			}
			catch (Exception e) {
				throw new ExecutionException("Could not open file", e);
			}
		}
		if (type == null) {
			type = "stdout";
		}
		type = type.toLowerCase();
		if (type.equals("stdout")) {
			return System.out;
		}
		if (type.equals("stderr")) {
			return System.err;
		}
		throw new ExecutionException("Invalid type: " + type);
	}

	public static final Arg PA_STREAM = new Arg.Positional("stream");

	static {
		setArguments("sys_closestream", new Arg[] { PA_STREAM });
	}

	public Object sys_closestream(VariableStack stack) throws ExecutionException {
		Object s = PA_STREAM.getValue(stack);
		try {
			if (s instanceof OutputStream) {
				((OutputStream) s).close();
			}
			if (s instanceof InputStream) {
				((InputStream) s).close();
			}
		}
		catch (IOException e) {
			logger.debug("Close failed", e);
		}
		return null;
	}

	static {
		setArguments("str_concat", new Arg[] { Arg.VARGS });
	}

	public String str_concat(VariableStack stack) throws ExecutionException {
		Object[] args = Arg.VARGS.asArray(stack);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buf.append(TypeUtil.toString(args[i]));
		}
		return buf.toString();
	}

	public static final Integer MINUS_ONE = new Integer(-1);

	public static final Arg PA_STRING = new Arg.Positional("string");
	public static final Arg PA_FROM = new Arg.Positional("from");
	public static final Arg OA_TO = new Arg.Optional("to", MINUS_ONE);

	static {
		setArguments("str_substring", new Arg[] { PA_STRING, PA_FROM, OA_TO });
	}

	public String str_substring(VariableStack stack) throws ExecutionException {
		String str = TypeUtil.toString(PA_STRING.getValue(stack));
		int from = TypeUtil.toInt(PA_FROM.getValue(stack));
		int to = TypeUtil.toInt(OA_TO.getValue(stack));
		if (to == -1) {
			return str.substring(from);
		}
		else {
			return str.substring(from, to);
		}
	}

	static {
		setArguments("sys_isdefined", new Arg[] { PA_NAME });
		setQuotedArgs("sys_isdefined");
	}

	public boolean sys_isdefined(VariableStack stack) throws ExecutionException {
		Identifier var = TypeUtil.toIdentifier(PA_NAME.getValue(stack));
		return stack.isDefined(var.getName());
	}

	static {
		setArguments("sys_istrue", new Arg[] { PA_VALUE });
	}

	public boolean sys_istrue(VariableStack stack) throws ExecutionException {
		return TypeUtil.toBoolean(PA_VALUE.getValue(stack));
	}

	static {
		setArguments("sys_islist", new Arg[] { PA_VALUE });
	}

	public boolean sys_islist(VariableStack stack) throws ExecutionException {
		return PA_VALUE.getValue(stack) instanceof List;
	}

	public static final Arg PA_REGEXP = new Arg.Positional("regexp");

	static {
		setArguments("str_matches", new Arg[] { PA_STRING, PA_REGEXP });
	}

	public boolean str_matches(VariableStack stack) throws ExecutionException {
		String string = TypeUtil.toString(PA_STRING.getValue(stack));
		String regexp = TypeUtil.toString(PA_REGEXP.getValue(stack));
		Pattern p = Pattern.compile(regexp, Pattern.DOTALL);
		return p.matcher(string).matches();
	}

	public static final Arg PA_SEPARATOR = new Arg.Positional("separator");

	static {
		setArguments("str_split", new Arg[] { PA_STRING, PA_SEPARATOR });
	}

	public List str_split(VariableStack stack) throws ExecutionException {
		String str = TypeUtil.toString(PA_STRING.getValue(stack));
		String sep = TypeUtil.toString(PA_SEPARATOR.getValue(stack));
		List list = new ArrayList();
		int index = -1;
		int last = 0;
		do {
			index = str.indexOf(sep, index + sep.length());
			if (index > -1) {
				if (last < index) {
					list.add(str.substring(last, index));
				}
				last = index + sep.length();
			}
		} while (index != -1);
		list.add(str.substring(last, str.length()));
		return list;
	}

	static {
		setArguments("str_quote", new Arg[] { PA_STRING });
	}

	public Object str_quote(VariableStack stack) throws ExecutionException {
		String str = TypeUtil.toString(PA_STRING.getValue(stack));
		return "\"" + str + "\"";
	}

	static {
		setArguments("sys_discard", new Arg[] { Arg.VARGS });
	}

	public void sys_discard(VariableStack stack) throws ExecutionException {
		/*
		 * This is supposed to not do anything Well, not quite. It takes
		 * arguments but it does not return them
		 */
	}

	public static final Arg PA_TO = new Arg.Positional("to");
	public static final Arg OA_STEP = new Arg.Optional("step", new Integer(1));

	static {
		setArguments("sys_range", new Arg[] { PA_FROM, PA_TO, OA_STEP });
	}

	public RangeIterator sys_range(VariableStack stack) throws ExecutionException {
		return new RangeIterator(TypeUtil.toInt(PA_FROM.getValue(stack)),
				TypeUtil.toInt(PA_TO.getValue(stack)), TypeUtil.toInt(OA_STEP.getValue(stack)));
	}

	public static final Arg OA_INVERT = new Arg.Optional("invert", Boolean.FALSE);

	static {
		setArguments("sys_filter", new Arg[] { PA_REGEXP, OA_INVERT, Arg.VARGS });
	}

	public Object sys_filter(VariableStack stack) throws ExecutionException {
		Pattern pattern = Pattern.compile(TypeUtil.toString(PA_REGEXP.getValue(stack)));
		boolean invert = TypeUtil.toBoolean(OA_INVERT.getValue(stack));
		Object[] args = Arg.VARGS.asArray(stack);
		ArrayList ret = new ArrayList();
		if (args.length > 1) {
			for (int i = 0; i < args.length; i++) {
				String value = TypeUtil.toString(args[i]);
				if (pattern.matcher(value).matches() ^ invert) {
					ret.add(value);
				}
			}
			return ret.toArray();
		}
		else if (args.length == 1) {
			Iterator i = TypeUtil.toIterator(args[0]);
			while (i.hasNext()) {
				String value = TypeUtil.toString(i.next());
				if (pattern.matcher(value).matches() ^ invert) {
					ret.add(value);
				}
			}
			return ret;
		}
		else {
			return null;
		}
	}

	public static final Arg OA_DESCENDING = new Arg.Optional("descending", Boolean.FALSE);

	static {
		setArguments("sys_sort", new Arg[] { OA_DESCENDING, Arg.VARGS });
	}

	public static final Comparator INVERSE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Comparable c2 = (Comparable) o2;
			// c1.compareTo(o2) == -c2.compareTo(o1)
			return c2.compareTo(o1);
		}
	};

	public Object sys_sort(VariableStack stack) throws ExecutionException {
		boolean descending = TypeUtil.toBoolean(OA_DESCENDING.getValue(stack));
		Object[] args = Arg.VARGS.asArray(stack);
		ArrayList ret = new ArrayList();
		if (args.length > 1) {
			for (int i = 0; i < args.length; i++) {
				ret.add(args[i]);
			}
			Object[] array = ret.toArray();
			try {
				if (descending) {
					Arrays.sort(array, INVERSE_COMPARATOR);
				}
				else {
					Arrays.sort(array);
				}
			}
			catch (ClassCastException e) {
				throw new ExecutionException("Cannot sort items of different types");
			}
			return array;
		}
		else if (args.length == 1) {
			Iterator i = TypeUtil.toIterator(args[0]);
			while (i.hasNext()) {
				ret.add(i.next());
			}
			Object[] array = ret.toArray();
			Arrays.sort(array);
			try {
				if (descending) {
					Arrays.sort(array, INVERSE_COMPARATOR);
				}
				else {
					Arrays.sort(array);
				}
			}
			catch (ClassCastException e) {
				throw new ExecutionException("Cannot sort items of different types");
			}
			return Arrays.asList(array);
		}
		else {
			return null;
		}
	}

	static {
		setArguments("sys_value", new Arg[] { PA_VALUE });
		setAcceptsInlineText("sys_value", true);
	}

	public Object sys_value(VariableStack stack) throws ExecutionException {
		return PA_VALUE.getValue(stack);
	}

	static {
		setArguments("str_nl", new Arg[] {});
	}

	public Object str_nl(VariableStack stack) throws ExecutionException {
		return "\n";
	}

	static {
		setArguments("str_strip", new Arg[] { PA_VALUE });
		setAcceptsInlineText("sys_strip", true);
	}

	public Object str_strip(VariableStack stack) throws ExecutionException {
		return TypeUtil.toString(PA_VALUE.getValue(stack)).trim();
	}

	static {
		setArguments("sys_dot", new Arg[] { Arg.VARGS });
	}

	public static class DotIterator implements KarajanIterator {
		private final Iterator[] its;
		private boolean next, nextValid;
		private int crt;

		public DotIterator(Iterator[] its) {
			this.its = its;
			crt = 0;
		}

		private void checkNext() {

		}

		public void remove() {
			throw new RuntimeException("Not implemented");
		}

		public boolean hasNext() {
			if (nextValid) {
				return next;
			}
			for (int i = 0; i < its.length; i++) {
				if (!its[i].hasNext()) {
					next = false;
					nextValid = true;
					return false;
				}
			}
			next = true;
			nextValid = true;
			return true;
		}

		public Object next() {
			List l = new LinkedList();
			for (int i = 0; i < its.length; i++) {
				l.add(its[i].next());
			}
			nextValid = false;
			crt++;
			return l;
		}

		public int current() {
			return crt;
		}

		public int count() {
			return -1;
		}

		public Object peek() {
			throw new UnsupportedOperationException("peek()");
		}

	}

	public Object sys_dot(VariableStack stack) throws ExecutionException {
		Object[] args = Arg.VARGS.asArray(stack);
		boolean iterator = false;
		Iterator[] its = new Iterator[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof Iterator) {
				iterator = true;
				its[i] = (Iterator) args[i];
			}
			else if (args[i] instanceof Collection) {
				its[i] = ((Collection) args[i]).iterator();
			}
			else if (args[i] instanceof FutureVariableArguments) {
				its[i] = ((FutureVariableArguments) args[i]).futureIterator();
				iterator = true;
			}
			else if (args[i] instanceof VariableArguments) {
				its[i] = ((VariableArguments) args[i]).iterator();
			}
			else {
				throw new ExecutionException("Not a vector: " + args[i]);
			}
		}
		if (iterator) {
			return new DotIterator(its);
		}
		else {
			List l = new LinkedList();
			boolean eoi = false;
			while (!eoi) {
				List item = new LinkedList();
				for (int i = 0; i < its.length; i++) {
					if (its[i].hasNext()) {
						item.add(its[i].next());
					}
					else {
						eoi = true;
						break;
					}
				}
				if (!eoi) {
					l.add(item);
				}
			}
			return l;
		}
	}

	static {
		setArguments("sys_cross", new Arg[] { Arg.VARGS });
	}

	public Object sys_cross(VariableStack stack) throws ExecutionException {
		Object[] args = Arg.VARGS.asArray(stack);
		List l = new LinkedList();
		cross(l, new LinkedList(), args);
		return l;
	}

	private void cross(List dest, List partial, Object[] args) throws ExecutionException {
		int index = partial.size();
		if (index >= args.length) {
			dest.add(partial);
			return;
		}
		if (args[index] instanceof Collection) {
			Iterator i = ((Collection) args[index]).iterator();
			while (i.hasNext()) {
				List lp = new LinkedList(partial);
				Object next = i.next();
				lp.add(next);
				cross(dest, lp, args);
			}
		}
		else {
			throw new ExecutionException("Not a vector: " + args[index]);
		}
	}

	public static final Arg PA_CODE = new Arg.Positional("code");

	static {
		setArguments("str_chr", new Arg[] { PA_CODE });
	}

	public char str_chr(VariableStack stack) throws ExecutionException {
		return (char) TypeUtil.toInt(PA_CODE.getValue(stack));
	}

	public static final Arg OA_ASMAP = new Arg.Optional("asmap", Boolean.FALSE);

	static {
		setArguments("sys_stats", new Arg[] { OA_ASMAP });
	}

	public Object sys_stats(VariableStack stack) throws ExecutionException {
		Runtime r = Runtime.getRuntime();
		r.gc();
		long memUsed = r.totalMemory() - r.freeMemory();
		long heapSize = r.totalMemory();
		long heapMax = r.maxMemory();
		int cpus = r.availableProcessors();
		int threads = Thread.activeCount();
		if (TypeUtil.toBoolean(OA_ASMAP.getValue(stack))) {
			java.util.Map m = new HashMap();
			m.put("memused", new Long(memUsed));
			m.put("heapsize", new Long(heapSize));
			m.put("heapmax", new Long(heapMax));
			m.put("cpucount", new Integer(cpus));
			m.put("threadcount", new Integer(threads));
			return m;
		}
		else {
			return "Used memory: " + formatMemSize(memUsed) + "\nHeap size: "
					+ formatMemSize(heapSize) + "\nMax heap: " + formatMemSize(heapMax)
					+ "\nNumber of CPUs: " + cpus + "\nActive threads: " + threads;
		}
	}

	private static final NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(true);
	}

	private String formatMemSize(double value) {
		if (value > 1000000) {
			return nf.format(value / 1024 / 1024) + " MB";
		}
		else if (value > 1000) {
			return nf.format(value / 1024) + " KB";
		}
		else {
			return nf.format(value) + " B";
		}
	}
}