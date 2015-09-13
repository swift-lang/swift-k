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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 9, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import k.rt.ExecutionException;
import k.rt.OutputStreamSink;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class FileWrite extends InternalFunction {
	private ArgRef<String> name;
	private ArgRef<Boolean> append;
	private Node body;
	private ChannelRef<Object> c_vargs;

	@Override
	protected Signature getSignature() {
		return new Signature(params("name", optional("append", Boolean.FALSE), block("body")));
	}

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var.Channel vargs = scope.addChannel("...");
		vargs.appendDynamic();
		c_vargs = scope.getChannelRef(vargs);
		super.compileBlocks(w, sig, blocks, scope);
	}

	@Override
	protected void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState(1);
		BufferedOutputStream os = (BufferedOutputStream) thr.popState();
		try {
			switch (i) {
				case 0:
					os = openStream(thr);
					c_vargs.set(thr.getStack(), new OutputStreamSink(os));
					i++;
				default:
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					closeStream(os);
			}
		}
		catch (Yield y) {
			y.getState().push(os);
			y.getState().push(i, 1);
			throw y;
		}
		catch (RuntimeException e) {
			if (os != null) {
				closeStream(os);
			}
			throw e;
		}
	}

	protected BufferedOutputStream openStream(LWThread thr) {
		Stack stack = thr.getStack();
		String name = this.name.getValue(stack);
		boolean append = this.append.getValue(stack);
		
		try {
			return new BufferedOutputStream(new FileOutputStream(name, append));
		}
		catch (FileNotFoundException e) {
			throw new ExecutionException(this, e);
		}
	}
	
	

	protected void closeStream(OutputStream os) {
		try {
			os.close();
		}
		catch (IOException e) {
			throw new ExecutionException(this, e);
		}
	}
}
