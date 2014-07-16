//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 7, 2012
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class OrderedChannelsNode extends CompoundNode {
	
	private List<ChannelRef<Object>> buffered;
	private List<ChannelRef<Object>> returns;
	
	private int frameSize;

	@Override
	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		List<TrackingScope> lts = new LinkedList<TrackingScope>();
		for (WrapperNode c : w.nodes()) {
			TrackingScope ts = new TrackingScope(scope);
			ts.setTrackChannels(true);
			ts.setAllowChannelReturns(true);
			ts.setTrackNamed(false);
			ts.setDelayedClosing(true);

			Node n = compileChild(c, ts);
			if (n != null) {
			    OrderedChannelsWrapper wr = newOrderedChannelsWrapper(n);
			    wr.setParent(this);
				addChild(wr);
				lts.add(ts);
			}
		}
		if (childCount() == 0) {
			return null;
		}
		else {
			processChannels(lts, scope);
			return this;
		}
	}

	protected OrderedChannelsWrapper newOrderedChannelsWrapper(Node n) {
		return new OrderedChannelsWrapper(n);
	}

	private void processChannels(List<TrackingScope> lts, Scope scope) {
		Map<String, Boolean> trackedChannels = new HashMap<String, Boolean>();
		for (TrackingScope ts : lts) {
			if (ts.getReferencedChannels() != null) {
				for (String cn : ts.getReferencedChannels().keySet()) {
					if (trackedChannels.containsKey(cn)) {
						trackedChannels.put(cn, Boolean.TRUE);
					}
					else {
						trackedChannels.put(cn, Boolean.FALSE);
					}
				}
			}
		}
		if (trackedChannels.isEmpty()) {
			return;
		}
		
		Map<String, ChannelRef.Ordered<Object>> prev = new HashMap<String, ChannelRef.Ordered<Object>>();
		int childIndex = 0;
		
		for (TrackingScope ts : lts) {
			if (ts.getReferencedChannels() != null) {
				for (String cn : ts.getReferencedChannels().keySet()) {
					if (trackedChannels.containsKey(cn)) {
						Var.Channel src = ts.getChannel(cn);
						Var.Channel dst = scope.lookupChannel(cn);
						ChannelRef<Object> dstref = scope.getChannelRef(dst);
						if (dst.isDynamic() && !dst.isCommutative()) {
							if (trackedChannels.get(cn)) {			
								ChannelRef.Ordered<Object> cr = new ChannelRef.Ordered<Object>(cn, src.getIndex(), dstref, prev.get(cn));
								getOCW(childIndex).addChannel(cr);
								prev.put(cn, cr);
							}
							else {
								// only referenced from one place, no buffering 
								getOCW(childIndex).addChannel(new ChannelRef.Redirect<Object>(cn, src.getIndex(), dstref));
							}
						}
					}
				}
			}
			childIndex++;
			ts.close();
		}
	}
	
	protected OrderedChannelsWrapper getOCW(int index) {
		return (OrderedChannelsWrapper) getChild(index);
	}
	
	protected void initializeBuffers(int childIndex, Stack stack) {
		getOCW(childIndex).initializeArgs(stack);
	}
}
