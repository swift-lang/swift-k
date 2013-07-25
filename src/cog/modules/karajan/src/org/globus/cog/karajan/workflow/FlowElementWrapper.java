// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.AdaptiveArrayList;
import org.globus.cog.karajan.util.AdaptiveMap;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.LoadListener;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.nodes.user.UDEWrapper;

public final class FlowElementWrapper implements FlowElement {
	private FlowElement peer, parent;
	private List<FlowElement> elements;
	private Map<String, Object> properties;
	private Map<String, Object> staticArguments;
	private String type;
	private boolean peerInitialized;

	private static AdaptiveMap.Context pc = new AdaptiveMap.Context(),
			sac = new AdaptiveMap.Context();
	private static AdaptiveArrayList.Context ec = new AdaptiveArrayList.Context();

	public FlowElementWrapper() {
		properties = new AdaptiveMap<String, Object>(pc);
		staticArguments = new AdaptiveMap<String, Object>(sac);
		elements = Collections.emptyList();
	}

	public void addElement(FlowElement element) {
		if (peer == null) {
			if (elements.isEmpty()) {
				elements = new AdaptiveArrayList<FlowElement>(ec);
			}
			elements.add(element);
		}
		else {
			peer.addElement(element);
		}
	}

	public void replaceElement(int index, FlowElement element) {
		if (peer == null) {
			elements.set(index, element);
		}
		else {
			peer.replaceElement(index, element);
		}
	}

	public void removeElement(int index) {
		if (peer == null) {
			elements.remove(index);
		}
		else {
			peer.removeElement(index);
		}
	}

	public FlowElement getElement(int index) {
		if (peer == null) {
			return elements.get(index);
		}
		else {
			return peer.getElement(index);
		}
	}

	public int elementCount() {
		if (peer == null) {
			return elements.size();
		}
		else {
			return peer.elementCount();
		}
	}

	public List<FlowElement> elements() {
		if (peer == null) {
			return elements;
		}
		else {
			return peer.elements();
		}
	}

	public void setElementType(String type) {
		this.type = type;
	}

	public String getElementType() {
		return type;
	}

	public void setProperty(String name, Object value) {
		if (peer == null) {
			properties.put(name.toLowerCase(), value);
		}
		else {
			peer.setProperty(name, value);
		}
	}

	public void removeProperty(String name) {
		if (peer == null) {
			properties.remove(name.toLowerCase());
		}
		else {
			peer.removeProperty(name);
		}
	}

	public void setProperty(final String name, final int value) {
		setProperty(name, new Integer(value));
	}

	public Object getProperty(final String name) {
		if (peer == null) {
			return properties.get(name.toLowerCase());
		}
		else {
			return peer.getProperty(name);
		}
	}

	public boolean hasProperty(final String name) {
		if (peer == null) {
			return properties.containsKey(name.toLowerCase());
		}
		else {
			return peer.hasProperty(name);
		}
	}

	public Collection<String> propertyNames() {
		if (peer == null) {
			return properties.keySet();
		}
		else {
			return peer.propertyNames();
		}
	}

	public void addStaticArgument(String name, Object value) {
		if (peer == null) {
			staticArguments.put(name.toLowerCase(), value);
		}
		else {
			peer.addStaticArgument(name, value);
		}
	}

	public void setStaticArguments(Map<String, Object> args) {
		throw new UnsupportedOperationException("setStaticArguments");
	}

	public Map<String, Object> getStaticArguments() {
		if (peer == null) {
			return staticArguments;
		}
		else {
			return peer.getStaticArguments();
		}
	}

	public void setParent(FlowElement parent) {
		this.parent = parent;
	}

	public FlowElement getParent() {
		if (parent instanceof FlowElementWrapper) {
			FlowElementWrapper few = (FlowElementWrapper) parent;
			if (few.getPeer() != null) {
				return few.getPeer();
			}
			else {
				return few;
			}
		}
		else {
			return parent;
		}
	}

	public FlowElement getPeer() {
		return peer;
	}

	public void failImmediately(VariableStack stack, ExecutionException e) throws ExecutionException {
		if (peer == null) {
			EventListener caller = stack.getCaller();
			caller.failed(stack, e);
		}
		else {
			peer.failImmediately(stack, e);
		}
	}

	public boolean acceptsInlineText() {
		return true;
	}
	
	public void start(VariableStack stack) throws ExecutionException {
		synchronized (this) {
			if (peer == null) {
				bind(stack);
			}
		}
		peer.start(stack);
	}
	
	public void abort(VariableStack stack) throws ExecutionException {
		peer.abort(stack);
	}

	public void restart(VariableStack stack) throws ExecutionException {
		peer.restart(stack);
	}

	public void completed(VariableStack stack) throws ExecutionException {
		peer.completed(stack);
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		peer.failed(stack, e);
	}

	public synchronized boolean resolve(VariableStack stack) throws ExecutionException {
		if (peer == null) {
			bind(stack);
			return true;
		}
		else {
			return false;
		}
	}

	private void bind(final VariableStack stack) throws ExecutionException {
		try {
			DefList.Entry def = DefUtil.getDef(stack, getElementType(), getParent());
			if (def == null || def.getDef() == null) {
				throw new ExecutionException("'" + getElementType() + "' is not defined.");
			}
			Object value = def.getDef();
			FlowElement peer;
			if (value instanceof UDEDefinition) {
				peer = new UDEWrapper((UDEDefinition) value);
			}
			else if (value instanceof JavaElement) {
				peer = ((JavaElement) value).newInstance();
			}
			else {
				throw new ExecutionException("Unrecognized definition for '" + getElementType()
						+ "': " + value);
			}
			peer.setElementType(def.getFullName());
			populate(peer);
			replaceElement(getParent(), this, peer);
			this.peer = peer;
			if (peer instanceof LoadListener) {
				((LoadListener) peer).loadComplete();
			}
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (KarajanRuntimeException e) {
			throw new ExecutionException("Karajan exception: " + e.getMessage(), e);
		}
	}

	private void replaceElement(FlowElement parent, FlowElementWrapper s,
			FlowElement d) {
		if (parent != null) {
			synchronized(parent) {
				List<FlowElement> l = parent.elements();
				for (int i = 0; i < l.size(); i++) {
					if (l.get(i) == s) {
						l.set(i, d);
						break;
					}
				}
			}
		}
	}

	protected void populate(final FlowElement peer) {
		peer.setParent(getParent());
		if (!peer.acceptsInlineText() && properties.containsKey(TEXT)) {
			properties.remove(TEXT);
		}
		peer.setProperties(properties);
		peer.setStaticArguments(staticArguments);
		peer.setElements(elements);
		elements = null;
		properties = null;
		staticArguments = null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getElementType());
		if (hasProperty("annotation")) {
			sb.append(" (");
			sb.append(getProperty("annotation"));
			sb.append(") ");
		}
		Object fileName = FlowNode.getTreeProperty(FILENAME, this);
		if (fileName instanceof String) {
			String fn = (String) fileName;
			fn = fn.substring(1 + fn.lastIndexOf('/'));
			sb.append(" @ ");
			sb.append(fn);

			if (hasProperty(LINE)) {
				sb.append(", line: ");
				sb.append(getProperty(LINE));
			}
		}
		return sb.toString();
	}

	public boolean isSimple() {
		if (peer != null) {
			return peer.isSimple();
		}
		else {
			return false;
		}
	}

	public void executeSimple(VariableStack stack) throws ExecutionException {
		synchronized (this) {
			if (peer == null) {
				bind(stack);
			}
		}
		peer.executeSimple(stack);
	}

	public void setElements(List<FlowElement> elements) {
		throw new UnsupportedOperationException("setElements");
	}

	public void setProperties(Map<String, Object> properties) {
		throw new UnsupportedOperationException("setProperties");
	}

	public void futureModified(Future f, VariableStack stack) {
		peer.futureModified(f, stack);
	}
}
