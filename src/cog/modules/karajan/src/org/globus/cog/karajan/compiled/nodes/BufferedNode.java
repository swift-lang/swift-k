//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 7, 2012
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
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class BufferedNode extends CompoundNode {
	
	private List<ChannelRef.Buffer<Object>> channelBuffers;
	private List<VarRef.Buffer<Object>> argBuffers;

	@Override
	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		TrackingScope cs = new TrackingScope(scope);
		cs.setTrackChannels(true);
		cs.setTrackNamed(true);
		cs.setFixedAllocation(true);
		
		Node fn = super.compileChildren(w, cs);
		
		Map<String, List<Node>> cl = cs.getReferencedChannels();
		
		if (cl != null) {
			channelBuffers = buildBufferedChannelRefs(cl.keySet(), cs);
		}
		
		Map<String, List<Node>> nl = cs.getReferencedParams();
		if (nl != null) {
			argBuffers = buildBufferedArgRefs(nl.keySet(), scope);
		}
				
		cs.close();
		return fn;
	}
		
	private List<ChannelRef.Buffer<Object>> buildBufferedChannelRefs(Collection<String> names, Scope scope) {
		List<ChannelRef.Buffer<Object>> l = null;
		if (names != null) {
			l = new LinkedList<ChannelRef.Buffer<Object>>();
			for (String name : names) {
				Var.Channel dst = scope.parent.lookupChannel(name);
				
				if (!dst.getNoBuffer()) {
					Var.Channel src = scope.lookupChannel(name);
					l.add(new ChannelRef.Buffer<Object>(name, src.getIndex(), dst.getIndex()));
				}
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
}
