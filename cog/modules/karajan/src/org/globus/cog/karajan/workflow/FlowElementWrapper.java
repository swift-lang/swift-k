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
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.nodes.ExtendedFlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;
import org.globus.cog.karajan.workflow.nodes.user.UDEWrapper;

public final class FlowElementWrapper implements ExtendedFlowElement {
	private FlowElement peer, parent;
	private List elements;
	private Map properties;
	private Map staticArguments;
	private String type;
	private boolean peerInitialized;
	private static AdaptiveMap.Context pc = new AdaptiveMap.Context(), sac = new AdaptiveMap.Context();
	private static AdaptiveArrayList.Context ec = new AdaptiveArrayList.Context();

	public FlowElementWrapper() {
		properties = new AdaptiveMap(pc);
		staticArguments = new AdaptiveMap(sac);
		elements = Collections.EMPTY_LIST;
	}

	public void addElement(FlowElement element) {
		if (peer == null) {
			if (elements.isEmpty()) {
				elements = new AdaptiveArrayList(ec);
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
			return (FlowElement) elements.get(index);
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

	public List elements() {
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

	public Collection propertyNames() {
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

	public void setStaticArguments(Map args) {
		throw new UnsupportedOperationException("setStaticArguments");
	}
	
	public Map getStaticArguments() {
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

	public void failImmediately(VariableStack stack, String message) throws ExecutionException {
		if (peer == null) {
			EventListener caller = (EventListener) stack.getVar(CALLER);
			EventBus.post(caller, new FailureNotificationEvent(this, stack, message, null));
		}
		else {
			peer.failImmediately(stack, message);
		}
	}

	public boolean acceptsInlineText() {
		return true;
	}

	public synchronized void event(final Event e) throws ExecutionException {
		if (peer == null) {
			bind(e.getStack());
		}
		peer.event(e);
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
		String tmp = getElementType();
		if (hasProperty("annotation")) {
			tmp = tmp + " (" + getProperty("annotation") + ") ";
		}
		Object fileName = FlowNode.getTreeProperty(FILENAME, this);
		if (fileName instanceof String) {
			String fn = (String) fileName;
			fn = fn.substring(1 + fn.lastIndexOf('/'));
			tmp = tmp + " @ " + fn;
		}
		if (hasProperty(LINE)) {
			tmp = tmp + ", line: " + getProperty(LINE);
		}
		return tmp;
	}

	public boolean isSimple() {
		if (peer != null) {
			if (peer instanceof ExtendedFlowElement) {
				return ((ExtendedFlowElement) peer).isSimple();
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	public void executeSimple(VariableStack stack) throws ExecutionException {
		if (peer == null) {
			synchronized (this) {
				if (peer == null) {
					bind(stack);
				}
			}
		}
		if (peer instanceof ExtendedFlowElement) {
			((ExtendedFlowElement) peer).executeSimple(stack);
		}
		else {
			throw new ExecutionException(
					"Internal error: executeDeterministic() called on standard element");
		}
	}

	public void setElements(List elements) {
		throw new UnsupportedOperationException("setElements");
	}

	public void setProperties(Map properties) {
		throw new UnsupportedOperationException("setProperties");
	}
}
