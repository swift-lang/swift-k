/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import k.rt.Channel;
import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;

public class Misc {

	private static long UIDIndex = System.currentTimeMillis();
	
	public static class FileContains extends AbstractSingleValuedFunction {
		private ArgRef<String> file;
		private ArgRef<String> value;
		private VarRef<Context> context;

		@Override
		protected void addLocals(Scope scope) {
			super.addLocals(scope);
			context = scope.getVarRef(Context.VAR_NAME);
		}

		@Override
		protected Param[] getParams() {
			return params("file", "value");
		}
		
		@Override
		public Object function(Stack stack) {
			File f = TypeUtil.toFile(file.getValue(stack), context.getValue(stack).getCWD());
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = null;
				String value = TypeUtil.toString(this.value.getValue(stack));
				do {
					line = br.readLine();
					if (line.indexOf(value) != -1) {
						return true;
					}
				} while (line != null);
			}
			catch (Exception e) {
				throw new ExecutionException(this, "Error reading file " + f, e);
			}
			return false;
		}
	}


	public static class Equals extends BinaryOp<Object, Boolean> {

		@Override
		protected Boolean value(Object v1, Object v2) {
			if (v1 instanceof Number) {
				try {
					Number n2 = TypeUtil.toNumber(v2);
					return ((Number) v1).doubleValue() == n2.doubleValue();
				}
				catch (Exception e) {
					return false;
				}
			}
			else if (v1 == null) {
				return v2 == null;
			}
			else {
				return compare(v1, v2);
			}
		}
	}
	
	protected static boolean compare(Object o1, Object o2) {
		if (o1 instanceof FutureObject) {
			o1 = ((FutureObject) o1).getValue();
		}
		if (o2 instanceof FutureObject) {
			o2 = ((FutureObject) o2).getValue();
		}
		if (o1 instanceof List) {
			if (o2 instanceof List) {
				List<?> l1 = (List<?>) o1;
				List<?> l2 = (List<?>) o2;
				if (l1.size() != l2.size()) {
					return false;
				}
				Iterator<?> i1 = l1.iterator();
				Iterator<?> i2 = l2.iterator();
				while (i1.hasNext()) {
					if (!compare(i1.next(), i2.next())) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		else if (o1 == null) {
			return o2 == null;
		}
		else {
			return o1.equals(o2);
		}
	}
	
	public static class NotEquals extends Equals {
		@Override
		protected Boolean value(Object v1, Object v2) {
			return !super.value(v1, v2);
		}
	}

	public static class FormatNumber extends AbstractSingleValuedFunction {
		private ArgRef<String> pattern;
		private ArgRef<Number> value;
	
		@Override
		protected Param[] getParams() {
			return params("pattern", "value");
		}
		
		@Override
		public Object function(Stack stack) {
			String pattern = this.pattern.getValue(stack);
			Number value = this.value.getValue(stack);
			
			DecimalFormat df = new DecimalFormat(pattern);
			return df.format(value.doubleValue());
		}
	}
	
	public static class FormatDate extends AbstractSingleValuedFunction {
		private ArgRef<String> pattern;
		private ArgRef<Object> value;
	
		@Override
		protected Param[] getParams() {
			return params("pattern", "value");
		}
		
		@Override
		public Object function(Stack stack) {
			String pattern = this.pattern.getValue(stack);
			Object value = this.value.getValue(stack);
			
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			return df.format(value);
		}
	}

	
	public static class FileRead extends AbstractSingleValuedFunction {
		private ArgRef<String> file;
		private VarRef<Context> context;

		@Override
		protected void addLocals(Scope scope) {
			super.addLocals(scope);
			context = scope.getVarRef(Context.VAR_NAME);
		}

		
		@Override
		protected Param[] getParams() {
			return params("file");
		}
		
		@Override
		public Object function(Stack stack) {
			File f = TypeUtil.toFile(file.getValue(stack), context.getValue(stack).getCWD());
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				StringBuilder text = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					text.append(line);
					text.append('\n');
					line = br.readLine();
				}
				br.close();
				return text.toString();
			}
			catch (Exception e) {
				throw new ExecutionException(this, e.getMessage());
			}
		}
	}
	
	public static class FileReadLine extends AbstractSingleValuedFunction {
		private ArgRef<String> file;
		private ArgRef<Number> offset;
		private VarRef<Context> context;

		@Override
		protected void addLocals(Scope scope) {
			super.addLocals(scope);
			context = scope.getVarRef(Context.VAR_NAME);
		}

		
		@Override
		protected Param[] getParams() {
			return params("file", "offset");
		}
		
		@Override
		public Object function(Stack stack) {
			File f = TypeUtil.toFile(file.getValue(stack), context.getValue(stack).getCWD());
			long offset = this.offset.getValue(stack).longValue();
			try {
				RandomAccessFile file = new RandomAccessFile(f, "r");
				try {
					file.seek(offset);
					return file.readLine();
				}
				finally {
					file.close();
				}
			}
			catch (Exception e) {
				throw new ExecutionException(this, e.getMessage());
			}
		}
	}

	public static class UID extends AbstractSingleValuedFunction {
		private ArgRef<String> prefix;
		private ArgRef<String> suffix;
		
		@Override
		protected Param[] getParams() {
			return params(optional("prefix", ""), optional("suffix", ""));
		}

		@Override
		public Object function(Stack stack) {
			String prefix = this.prefix.getValue(stack);
			String suffix = this.suffix.getValue(stack);
			return nextUID(prefix, suffix);
		}
		
		public static String nextUID() {
		    return nextUID("", "");
		}

		public static String nextUID(String prefix, String suffix) {
			long index;
            synchronized (Misc.class) {
                index = UIDIndex++;
            }
            return prefix + alphanum(index) + suffix;
		}
	}


	public static final String codes = "0123456789abcdefghijklmnopqrstuvxyz";

	protected static String alphanum(long val) {
		StringBuffer sb = new StringBuffer();
		int base = codes.length();
		while (val > 0) {
			int c = (int) (val % base);
			sb.append(codes.charAt(c));
			val = val / base;
		}
		return sb.toString();
	}
	
	public static class Property extends InternalFunction {
		private ArgRef<String> name;
		private ArgRef<Object> value;
		private ChannelRef<Object> cr_properties;
		
		@Override
		protected Signature getSignature() {
			return new Signature(
					params("name", "value"),
					returns(channel("properties", 1))
			);
		}

		@Override
		protected void runBody(LWThread thr) {
			Stack stack = thr.getStack();
			cr_properties.append(stack,
					new org.globus.cog.karajan.util.Property(name.getValue(stack), value.getValue(stack)));
		}
	}
	
	public static class GetEnv extends AbstractSingleValuedFunction {
		private ArgRef<String> name;
	
		@Override
		protected Param[] getParams() {
			return params("name");
		}
		
		@Override
		public Object function(Stack stack) {
			return System.getenv(name.getValue(stack));
		}
	}
	
	public static class ContextAttribute extends AbstractSingleValuedFunction {
        private ArgRef<String> name;
        private VarRef<Context> context;
    
        @Override
        protected Param[] getParams() {
            return params("name");
        }
        
        @Override
		protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
				throws CompilationException {
            context = scope.getVarRef("#context");
			return super.compileBody(w, argScope, scope);
		}

		@Override
        public Object function(Stack stack) {
            return context.getValue(stack).getAttribute(name.getValue(stack));
        }
    }
	
	public static class OutputStream extends AbstractSingleValuedFunction {
		private ArgRef<String> type;
		private ArgRef<String> file;
		private VarRef<Context> context;

		@Override
		protected void addLocals(Scope scope) {
			super.addLocals(scope);
			context = scope.getVarRef(Context.VAR_NAME);
		}

	
		@Override
		protected Param[] getParams() {
			return params(optional("type", "stdout"), optional("file", null));
		}
		
		@Override
		public Object function(Stack stack) {
			String type = this.type.getValue(stack);
			if ("file".equals(type)) {
				String file = this.file.getValue(stack);
				if (file == null) {
					throw new ExecutionException(this, "Missing file argument");
				}
				try {
					return new PrintStream(new FileOutputStream(TypeUtil.toFile(file, context.getValue(stack).getCWD())));
				}
				catch (FileNotFoundException e) {
					throw new ExecutionException(this, "Failed to open stream", e);
				}
			}
			if (type.equals("stdout")) {
				return System.out;
			}
			if (type.equals("stderr")) {
				return System.err;
			}
			throw new ExecutionException(this, "Invalid type: " + type);
		}
	}

	public static class CloseStream extends InternalFunction {
		private ArgRef<Object> stream;
		
		@Override
		protected Signature getSignature() {
			return new Signature(params("stream"));
		}

		@Override
		protected void runBody(LWThread thr) {
			Stack stack = thr.getStack();
			Object s = stream.getValue(stack);
			try {
				if (s instanceof java.io.OutputStream) {
					((java.io.OutputStream) s).close();
				}
				else if (s instanceof java.io.InputStream) {
					((java.io.InputStream) s).close();
				}
				else {
					throw new ExecutionException(this, "Not a stream: " + s);
				}
			}
			catch (IOException e) {
				throw new ExecutionException(this, "Failed to close stream", e);
			}
		}
	}
	
	public static class Discard extends InternalFunction {
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Signature getSignature() {
			return new Signature(params("..."));
		}

		@Override
		protected void runBody(LWThread thr) {
		}
	}


	public static class Range extends AbstractSingleValuedFunction {
		private ArgRef<Number> from;
		private ArgRef<Number> to;
		private ArgRef<Number> step;
		
		@Override
		protected Param[] getParams() {
			return params("from", "to", optional("step", 1));
		}

		@Override
		public Object function(Stack stack) {
			return new org.globus.cog.karajan.util.Range(from.getValue(stack).intValue(),
				to.getValue(stack).intValue(), step.getValue(stack).intValue());
		}
	}

	
	public static class Filter extends InternalFunction {
		private ArgRef<String> pattern;
		private ArgRef<Boolean> invert;
		private ChannelRef<Object> c_vargs;
		private ChannelRef<Object> cr_vargs;
		
		@Override
		protected Signature getSignature() {
			return new Signature(
					params("pattern", optional("invert", Boolean.FALSE), "..."),
					returns(channel("...", DYNAMIC))
			);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void runBody(LWThread thr) {
			Stack stack = thr.getStack();
			Pattern pattern = Pattern.compile(this.pattern.getValue(stack));
			boolean invert = this.invert.getValue(stack).booleanValue();
			List<Object> l = c_vargs.get(stack).getAll();
			Channel<Object> r = cr_vargs.get(stack); 
			if (l.size() == 1 && l.get(0) instanceof List) {
				l = (List<Object>) l.get(0);
				List<String> lr = new ArrayList<String>();
				for (Object o : l) {
					String value = String.valueOf(o);
					if (pattern.matcher(value).matches() ^ invert) {
						lr.add(value);
					}
				}
				r.add(lr);
			}
			else {
				for (Object o : l) {
					String value = String.valueOf(o);
					if (pattern.matcher(value).matches() ^ invert) {
						r.add(value);
					}
				}
			}
		}
	}
	
	public static class Sort extends InternalFunction {
		private ArgRef<Boolean> descending;
		private ChannelRef<Object> c_vargs;
		private ChannelRef<Object> cr_vargs;
		
		@Override
		protected Signature getSignature() {
			return new Signature(
					params(optional("descending", Boolean.FALSE), "..."),
					returns("...")
			);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void runBody(LWThread thr) {
			Stack stack = thr.getStack();
			boolean descending = this.descending.getValue(stack).booleanValue();
			List<Object> l = c_vargs.get(stack).getAll();
			Channel<Object> r = cr_vargs.get(stack);
			
			if (l.size() == 1 && l.get(0) instanceof List) {
				l = (List<Object>) l.get(0);
				List<Object> lr = new ArrayList<Object>();
				lr.addAll(l);
				Collections.sort(lr, descending ? INVERSE_COMPARATOR : COMPARATOR);
				r.add(lr);
			}
			else {
				List<Object> lr = new ArrayList<Object>();
				lr.addAll(l);
				Collections.sort(lr, descending ? INVERSE_COMPARATOR : COMPARATOR);
				r.addAll(lr);
			}
		}
	}
	
	public static final Comparator<Object> INVERSE_COMPARATOR = new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			@SuppressWarnings("unchecked")
			Comparable<Object> c2 = (Comparable<Object>) o2;
			// c1.compareTo(o2) == -c2.compareTo(o1)
			return c2.compareTo(o1);
		}
	};
	
	public static final Comparator<Object> COMPARATOR = new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			@SuppressWarnings("unchecked")
			Comparable<Object> c1 = (Comparable<Object>) o1;
			return c1.compareTo(o2);
		}
	};
		
	public static class DotIterator implements Iterator<Object> {
		private final Iterator<Object>[] its;
		private boolean next, nextValid;
		private int crt;

		public DotIterator(Iterator<Object>[] its) {
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
			List<Object> l = new LinkedList<Object>();
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
	
	public static class Dot extends AbstractMultiValuedFunction {
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Signature getSignature() {
			return new Signature(params("..."));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Object function(Stack stack) {
			Object[] args = c_vargs.get(stack).toArray();
			boolean iterator = false;
			Iterator<Object>[] its = new Iterator[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Iterator) {
					iterator = true;
					its[i] = (Iterator<Object>) args[i];
				}
				else if (args[i] instanceof Iterable) {
					its[i] = ((Iterable<Object>) args[i]).iterator();
				}
				else {
					throw new ExecutionException("Not a vector: " + args[i]);
				}
			}
			if (iterator) {
				return new DotIterator(its);
			}
			else {
				List<List<Object>> l = new LinkedList<List<Object>>();
				boolean eoi = false;
				while (!eoi) {
					List<Object> item = new LinkedList<Object>();
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
	}

	public static class Cross extends AbstractMultiValuedFunction {
		private ChannelRef<Object> c_vargs;
		
		@Override
		protected Signature getSignature() {
			return new Signature(params("..."));
		}

		@Override
		public Object function(Stack stack) {
			Object[] args = c_vargs.get(stack).toArray();
			List<List<Object>> l = new LinkedList<List<Object>>();
			cross(l, new LinkedList<Object>(), args);
			return l;
		}
	}

	
	@SuppressWarnings("unchecked")
	private static void cross(List<List<Object>> dest, List<Object> partial, Object[] args) throws ExecutionException {
		int index = partial.size();
		if (index >= args.length) {
			dest.add(partial);
			return;
		}
		if (args[index] instanceof Collection) {
			Iterator<Object> i = ((Collection<Object>) args[index]).iterator();
			while (i.hasNext()) {
				List<Object> lp = new LinkedList<Object>(partial);
				Object next = i.next();
				lp.add(next);
				cross(dest, lp, args);
			}
		}
		else {
			throw new ExecutionException("Not a vector: " + args[index]);
		}
	}
	
	
	public static class Stats extends AbstractSingleValuedFunction {
		private ArgRef<Boolean> asMap;
	
		@Override
		protected Param[] getParams() {
			return params(optional("asMap", Boolean.FALSE));
		}
		
		@Override
		public Object function(Stack stack) {
			Runtime r = Runtime.getRuntime();
			r.gc();
			long memUsed = r.totalMemory() - r.freeMemory();
			long heapSize = r.totalMemory();
			long heapMax = r.maxMemory();
			int cpus = r.availableProcessors();
			int threads = Thread.activeCount();
			if (asMap.getValue(stack)) {
				java.util.Map<String, Number> m = new HashMap<String, Number>();
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
	}
	
	private static final NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(true);
	}

	private static String formatMemSize(double value) {
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