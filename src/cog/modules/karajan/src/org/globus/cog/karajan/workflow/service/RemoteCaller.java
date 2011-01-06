//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 24, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.EventCommand;

public class RemoteCaller implements FlowElement {
	private final InstanceContext instanceContext;
	private final int uid;

	public RemoteCaller(InstanceContext instanceContext, int uid) {
		this.instanceContext = instanceContext;
		this.uid = uid;
	}

	public void addElement(FlowElement element) {
		throw new UnsupportedOperationException();
	}
	
	public void replaceElement(int index, FlowElement element) {
		throw new UnsupportedOperationException();
	}
	
	public void removeElement(int index) {
		throw new UnsupportedOperationException();
	}

	public FlowElement getElement(int index) {
		throw new UnsupportedOperationException();
	}

	public int elementCount() {
		throw new UnsupportedOperationException();
	}

	public List elements() {
		throw new UnsupportedOperationException();
	}

	public void setElementType(String type) {
		throw new UnsupportedOperationException();
	}

	public String getElementType() {
		throw new UnsupportedOperationException();
	}

	public void setProperty(String name, Object value) {
		throw new UnsupportedOperationException();
	}

	public void removeProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public Object getProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public boolean hasProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public Collection propertyNames() {
		throw new UnsupportedOperationException();
	}

	public void setParent(FlowElement parent) {
		throw new UnsupportedOperationException();
	}

	public FlowElement getParent() {
		throw new UnsupportedOperationException();
	}

	public void failImmediately(VariableStack stack, String message) throws ExecutionException {
		event(new FailureNotificationEvent(this, stack, message, null));
	}

	public ProjectNode getProjectNode() {
		throw new UnsupportedOperationException();
	}

	public boolean acceptsInlineText() {
		throw new UnsupportedOperationException();
	}

	public void event(Event e) throws ExecutionException {
		try {
			KarajanChannel channel = ChannelManager.getManager().reserveChannel(
					instanceContext.getChannelContext());
			// after this it should be empty
			((NotificationEvent) e).setStack(null);
			e.setFlowElement(null);
			EventCommand cmd = new EventCommand(instanceContext, uid, e);
			cmd.executeAsync(channel);
			ChannelManager.getManager().releaseChannel(channel);
		}
		catch (Exception ex) {
			throw new ExecutionException(ex);
		}
	}

	public void setElements(List elements) {
		throw new UnsupportedOperationException();
	}

	public void setProperties(Map properties) {
		throw new UnsupportedOperationException();
	}

	public void addStaticArgument(String name, Object value) {
	}

	public void setStaticArguments(Map args) {
	}

	public Map getStaticArguments() {
		throw new UnsupportedOperationException();
	}
}
