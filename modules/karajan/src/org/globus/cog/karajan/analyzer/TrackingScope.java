//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 8, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.compiled.nodes.Node;


public class TrackingScope extends Scope {
	private boolean trackNamed = true, trackChannels = true;
	private boolean allowChannelReturns = true, allowNamedReturns = true;
	private boolean fixedAllocation = false;
	private boolean updateableChannelRefs = false;
	private boolean delayedClosing = false;
	
	private Map<String, List<ChannelRef<?>>> channelRefs;
	
	private Map<String, List<Node>> referencedChannels;
	private Map<String, List<Node>> referencedParams;
	
	private List<Var> closedChildVars;
	
	public TrackingScope(Scope scope) {
		super(scope);
	}
	
	@Override
	protected Var lookupParamRecursive(String name, Node src) {
		if (trackNamed) {
			referencedParams = add(referencedParams, name, src);
			return addParamWithSrc(name, src);
		}
		else {
			return super.lookupParamRecursive(name, src);
		}
	}

	private Var addParamWithSrc(String name, Node src) {
		if (this.allowNamedReturns) {
			super.lookupParamRecursive(name, src);
		}
		return addVar(name);
	}

	@Override
	protected Var.Channel lookupChannelRecursive(String name, Node src) {
		if (trackChannels) {
			if (allowChannelReturns) {
				Var.Channel prev = super.lookupChannelRecursive(name, src);
				if (prev.getNoBuffer()) {
					return prev;
				}
			}
			if (referencedChannels != null && referencedChannels.containsKey(name)) {
				return this.getChannel(name);
			}
			referencedChannels = add(referencedChannels, name, src);
			if (fixedAllocation) {
				return addChannelFixed(name, src);
			}
			else {
				return addChannel(name, src);
			}
		}
		else {
			return super.lookupChannelRecursive(name, src);
		}
	}
		
	@Override
	protected <T> ChannelRef<T> getChannelRefRecursive(String name, int frame) {
		ChannelRef<T> ref = super.getChannelRefRecursive(name, frame);
		if (updateableChannelRefs) {
			if (channelRefs == null) {
				channelRefs = new HashMap<String, List<ChannelRef<?>>>();
			}
			List<ChannelRef<?>> l = channelRefs.get(name);
			if (l == null) {
				l = new LinkedList<ChannelRef<?>>();
				channelRefs.put(name, l);
			}
			l.add(ref);
		}
		return ref;
	}

	public Var.Channel addChannel(String name, Node src) {
		if (allowChannelReturns) {
			return addChannelWrapper(name, parent.lookupChannel(name, src));
		}
		else {
			return addChannel(name, new StaticNullChannel());
		}
	}
	
	private Var.Channel addChannelWrapper(String name, Var.Channel prev) {
		Var.Channel v = new TrackingChannelWrapper(name, prev);
		addVar(v);
		return v;
	}
	
	public Var.Channel getChannel(String name) {
		Var existing = vars.get("#channel#" + name);
		if (existing == null) {
			throw new IllegalArgumentException("No such channel in this tracking scope: " + name);
		}
		return (Var.Channel) existing;
	}
	
	public Var getParam(String name) {
		 Var existing = vars.get("#param#" + name);
         if (existing == null) {
        	 throw new IllegalArgumentException("No such parameter in this tracking scope: " + name);
         }
         return existing;
	}

	public Var.Channel addChannelFixed(String name, Node src) {
		if (allowChannelReturns) {
			return addChannelWrapperFixed(name, parent.lookupChannel(name, src));
		}
		else {
			return addChannelFixed(name, new StaticNullChannel());
		}
	}
	
	public Var.Channel addChannelFixed(String name, Object value) {
		Var.Channel v = new Var.Channel(name);
		v.setValue(value);
		addVarFixed(v);
		return v;
	}
	
	public Var.Channel addChannelWrapperFixed(String name, Var.Channel prev) {
		Var.Channel v = new TrackingChannelWrapper(name, prev);
		addVarFixed(v);
		return v;
	}
	
	public void addVarFixed(Var ref) {
		if (vars == null) {
			vars = new HashMap<String, Var>();
		}
		if (!vars.containsKey(ref.name)) {
			int index = getContainerScope().allocateFixed(ref);
			ref.setIndex(index);
			vars.put(ref.name, ref);
		}
		else {
			Var existing = vars.get(ref.name);
			ref.setIndex(existing.getIndex());
		}
	}

	private Map<String, List<Node>> add(Map<String, List<Node>> m, String name, Node src) {
		if (m == null) {
			m = new HashMap<String, List<Node>>();
		} 
		List<Node> l = m.get(name);
		if (l == null) {
		    l = new LinkedList<Node>();
		    m.put(name, l);
		}
		if (src != null) {
			l.add(src);
		}
		return m;
	}

	private Set<String> add(Set<String> s, String name) {
		if (s == null) {
			s = new HashSet<String>();
		}
		if (!s.contains(name)) {
			s.add(name);
		}
		return s;
	}
	
	

	@Override
	protected void releaseVars(Collection<Var> c) {
		if (delayedClosing) {
			if (closedChildVars == null) {
				closedChildVars = new ArrayList<Var>();
			}
			closedChildVars.addAll(c);
		}
		else {
			super.releaseVars(c);
		}
	}
	
	@Override
	public void close() {
		if (closedChildVars != null) {
			super.releaseVars(closedChildVars);
		}
		super.close();
	}

	public Map<String, List<Node>> getReferencedChannels() {
		return referencedChannels;
	}

	public Map<String, List<Node>> getReferencedParams() {
		return referencedParams;
	}

	public boolean getTrackNamed() {
		return trackNamed;
	}

	public void setTrackNamed(boolean trackNamed) {
		this.trackNamed = trackNamed;
	}

	public boolean getTrackChannels() {
		return trackChannels;
	}

	public void setTrackChannels(boolean trackChannels) {
		this.trackChannels = trackChannels;
	}

	public boolean getAllowChannelReturns() {
		return allowChannelReturns;
	}

	public void setAllowChannelReturns(boolean allowChannelReturns) {
		this.allowChannelReturns = allowChannelReturns;
	}

	public boolean getAllowNamedReturns() {
		return allowNamedReturns;
	}

	public void setAllowNamedReturns(boolean allowNamedReturns) {
		this.allowNamedReturns = allowNamedReturns;
	}

	public boolean getFixedAllocation() {
		return fixedAllocation;
	}

	public void setFixedAllocation(boolean fixedAllocation) {
		this.fixedAllocation = fixedAllocation;
	}

	public boolean getDelayedClosing() {
		return delayedClosing;
	}

	public void setDelayedClosing(boolean delayedClosing) {
		this.delayedClosing = delayedClosing;
	}

	@Override
	protected String getType() {
		return "T";
	}
	
	private static class TrackingChannelWrapper extends Var.Channel {
		private Var.Channel prev;
		
		public TrackingChannelWrapper(String name, Var.Channel prev) {
			super(name);
			this.prev = prev;
		}

		@Override
		public boolean append(Object o) {
			return prev.append(o);
		}

		@Override
		public Object getValue() {
			return prev.getValue();
		}

		@Override
		public void setValue(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDynamic() {
			prev.setDynamic();
		}

		@Override
		public String toString() {
			return "TW[ " + prev + "]";
		}

		@Override
		public void appendDynamic() {
			prev.appendDynamic();
		}

		@Override
		public StaticChannel getChannel() {
			return prev.getChannel();
		}

		@Override
		public List<Object> getAll() {
			return prev.getAll();
		}

		@Override
		public int size() {
			return prev.size();
		}

		@Override
		public void disable() {
			prev.disable();
		}

		@Override
		public boolean isDisabled() {
			return prev.isDisabled();
		}

		@Override
		public boolean isSingleValued() {
			return prev.isSingleValued();
		}

		@Override
		public void setSingleValued(boolean singleValued) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getChannelName() {
			return prev.getChannelName();
		}

		@Override
		public boolean isDynamic() {
			return prev.isDynamic();
		}

		@Override
		public void setCanBeNull(boolean canBeNull) {
			throw new UnsupportedOperationException();
		}
	}
}
