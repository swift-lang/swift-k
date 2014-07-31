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

package org.globus.cog.karajan.compiled.nodes;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.WeakHashMap;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.RootScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.analyzer.Scope.JavaDef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.parser.NativeParser;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.KarajanProperties;

public class ExecuteFile extends InternalFunction {
	public static final Logger logger = Logger.getLogger(ExecuteFile.class);
	
	private static class Entry {
		public DynamicMain compiled;
		public long mtime;
		public FutureObject lock;
		public int varCount;
		
		public Entry() {
		}
	}
	
	private static Map<String, Entry> cache = new WeakHashMap<String, Entry>(); 
	
	private ArgRef<String> file;
	private ArgRef<String> namespace;
	private ChannelRef.Return<Object> cr_vargs;
	private ChannelRef.Return<Object> cr_stdout;
	private ChannelRef.Return<Object> cr_stderr;
	
	private VarRef<KarajanProperties> props;
	private VarRef<String> fileDir;
	private VarRef<Context> context;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params("file", optional("namespace", "")),
				returns(channel("...", DYNAMIC), channel("stderr", DYNAMIC), channel("stdout", DYNAMIC))
		);
	}
	
	@Override
	public Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
		props = scope.getVarRef("#properties");
		fileDir = scope.getVarRef("#filedir");
		context = scope.getVarRef("#context");
		return this;
	}

	public void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		Entry e = (Entry) thr.popState();
		Stack stack = thr.getStack();
		try {
			if (i == 0) {
				String iname = file.getValue(stack);
				String nsprefix = namespace.getValue(stack);
		        e = compile(stack, iname, nsprefix);
		        k.rt.Channel<Object> rvargs = cr_vargs.get(stack);
		        k.rt.Channel<Object> rstdout = cr_stdout.get(stack);
		        k.rt.Channel<Object> rstderr = cr_stderr.get(stack);
		        stack.enter(e.compiled, e.varCount);
		        e.compiled.bindChannels(rvargs, rstdout, rstderr, stack);
				i++;
			}
			e.compiled.run(thr);
			stack.leave();
		}
		catch (Yield y) {
			y.getState().push(e);
			y.getState().push(i);
		}
	}

	public Entry compile(Stack stack, String iname, String nsprefix) {
		Entry e = getCachedEntry(iname);
		if (e == null) {
			e = getCachedEntry(iname);
			Entry ne = actualCompile(stack, iname, nsprefix);
			e.compiled = ne.compiled;
			e.varCount = ne.varCount;
			e.lock.setValue(Boolean.TRUE);
		}
		else {
			e.lock.getValue();
		}
		return e;
	}

	private Entry actualCompile(Stack stack, String iname, String nsprefix) {
		try {
			Entry e = new Entry();
			WrapperNode wn = new NativeParser(iname, new FileReader(iname)).parse();
			wn.setProperty(WrapperNode.FILENAME, iname);
			RootScope rs = new RootScope(props.getValue(stack),
					new File(fileDir.getValue(stack), iname).getAbsolutePath(), context.getValue(stack));
			rs.addDef("k", "main", new JavaDef(DynamicMain.class));
			e.compiled = (DynamicMain) wn.compile(this, rs);
			e.varCount = rs.size();
			return e;
		}
		catch (Exception e) {
			throw new ExecutionException(this, "Could not load file " + iname, e);
		}
	}

	private synchronized static Entry getCachedEntry(String iname) {
		Entry e = cache.get(iname);
		long mtime = new File(iname).lastModified();
		if (e == null) {
			e = new Entry();
			e.lock = new FutureObject();
			e.mtime = mtime;
			cache.put(iname, e);
			e = null;
		}
		else {
			if (mtime > e.mtime) {
				e.compiled = null;
				e.lock = new FutureObject();
				e = null;
			}
		}
		return e;
	}
}