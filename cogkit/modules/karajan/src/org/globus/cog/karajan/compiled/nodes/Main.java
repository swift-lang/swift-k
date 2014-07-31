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
 * Created on Jun 6, 2003
 *
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import k.rt.NullSink;
import k.rt.Stack;
import k.rt.StdSink.StderrSink;
import k.rt.StdSink.StdoutNLSink;
import k.rt.StdSink.StdoutSink;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class Main extends FramedInternalFunction {
	public static final Logger logger = Logger.getLogger(Main.class);
	
	private ChannelRef<Object> c_stdout;
	private ChannelRef<Object> c_stderr;
	private ChannelRef<Object> c_export;
	private ChannelRef<Object> c_vargs;
	
	private Var args;
	
	private String fileName;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
			params(
				channel("stdout"), 
				channel("stderr"), 
				channel("export"),
				channel("...")
			)
		);
	}

	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Scope cs = new Scope(w, scope);
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		fileName = (String) WrapperNode.getTreeProperty(WrapperNode.FILENAME, w);
		
		scope.addVar(Namespace.VAR_NAME, "");
		args = scope.addVar("...");
		
		Node fn = compileChildren(w, cs);
		cs.close();
		setVarCount(cs.parent.size());
		return fn;
	}

	@Override
	protected void initializeArgs(Stack stack) {
		super.initializeArgs(stack);
		c_stdout.set(stack, new StdoutSink());
		c_stderr.set(stack, new StderrSink());
		c_export.set(stack, new NullSink());
		c_vargs.set(stack, new StdoutNLSink());
	}

	
	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		
		Map<String, String> env = System.getenv();
		for(Map.Entry<String, String> e : env.entrySet()) {
			//scope.addVar("env." + e.getKey(), e.getValue()); 
		}
		fileName = (String) WrapperNode.getTreeProperty(WrapperNode.FILENAME, w);
		
		return this;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setArgs(Collection<String> args) {
		this.args.setValue(args);
	}

	public void dump(File file) throws IOException {
		PrintStream ps = new PrintStream(new FileOutputStream(file));
		dump(ps, 0);
		ps.close();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}