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
 * Created on Jan 2, 2013
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class BufferedInternalFunction extends InternalFunction {
	
	private List<ChannelRef.Buffer<Object>> channelBuffers;
	private List<VarRef.Buffer<Object>> argBuffers;

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		TrackingScope cs = new TrackingScope(scope);
		cs.setTrackChannels(true);
		cs.setTrackNamed(true);
		cs.setFixedAllocation(true);
		
		super.compileBlocks(w, sig, blocks, cs);
		
		Map<String, List<Node>> cl = cs.getReferencedChannels();
        
        if (cl != null) {
            channelBuffers = buildBufferedChannelRefs(cl.keySet(), cs);
        }
        
        Map<String, List<Node>> nl = cs.getReferencedParams();
        if (nl != null) {
            argBuffers = buildBufferedArgRefs(nl.keySet(), scope);
        }
					
		cs.close();
	}
	
	private List<ChannelRef.Buffer<Object>> buildBufferedChannelRefs(Collection<String> names, Scope scope) {
		List<ChannelRef.Buffer<Object>> l = null;
		if (names != null) {
			l = new LinkedList<ChannelRef.Buffer<Object>>();
			for (String name : names) {
				Var.Channel dst = scope.parent.lookupChannel(name);
				Var.Channel src = scope.lookupChannel(name);
				
				l.add(new ChannelRef.Buffer<Object>(name, src.getIndex(), dst.getIndex()));
			}
		}
		return l;
	}
	
	private List<VarRef.Buffer<Object>> buildBufferedArgRefs(Collection<String> names, Scope scope) {
		List<VarRef.Buffer<Object>> l = null;
		if (names != null) {
			l = new LinkedList<VarRef.Buffer<Object>>();
			for (String name : names) {
				Var dst = scope.parent.lookupParam(name);
				Var src = scope.lookupParam(name);
				
				
				l.add(new VarRef.Buffer<Object>(name, src.getIndex(), dst.getIndex()));
			}
		}
		return l;
	}
	
	protected void addBuffers(Stack stack) {
		if (channelBuffers != null) {
			for (ChannelRef.Buffer<Object> b : channelBuffers) {
				b.create(stack);
			}
		}
	}

	protected void commitBuffers(Stack stack) {
		if (channelBuffers != null) {
			for (ChannelRef.Buffer<Object> b : channelBuffers) {
				b.commit(stack);
			}
		}
		if (argBuffers != null) {
			for (VarRef.Buffer<Object> b : argBuffers) {
				b.commit(stack);
			}
		}
	}

	protected List<ChannelRef.Buffer<Object>> getChannelBuffers() {
		return channelBuffers;
	}

	protected List<VarRef.Buffer<Object>> getArgBuffers() {
		return argBuffers;
	}
}
