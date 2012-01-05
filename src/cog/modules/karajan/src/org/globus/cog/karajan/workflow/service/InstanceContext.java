//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 1, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class InstanceContext {
	private String clientID, serverID;
	private ElementTree tree;
	private final UserContext userContext;
	private VariableStack stack;
	private String name;
	private Set runs;

	public InstanceContext(UserContext userContext) {
		this.userContext = userContext;
		if (userContext == null) {
			throw new RuntimeException("userContext is null");
		}
		runs = new HashSet();
	}

	public String getClientID() {
		return clientID;
	}
	
	public void setClientID(String id) {
		this.clientID = id;
	}

	public void setServerID(String id) {
		this.serverID = id;
		userContext.registerInstanceContext(this);
	}

	public String getServerID() {
		return serverID;
	}

	public String getID() {
		return clientID + "-" + serverID;
	}

	public UserContext getUserContext() {
		return userContext;
	}
	
	public ChannelContext getChannelContext() {
		return userContext.getChannelContext();
	}

	public VariableStack getStack() {
		return stack;
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
	}

	public ElementTree getTree() {
		return tree;
	}

	public void setTree(ElementTree tree) {
		this.tree = tree;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void destroy() {
		userContext.removeInstanceContext(this);
	}
	
	private static int ids = 0;
	
	public void registerExecutionContext(ExecutionContext ec) {
		synchronized(runs) {
			ec.setId(ids++);
			runs.add(ec);
		}
	}
	
	public Collection getExecutionContexts() {
		synchronized(runs) {
			return new ArrayList(runs);
		}
	}
	
	public void unregisterExecutionContext(ExecutionContext ec) {
		synchronized(runs) {
			runs.remove(ec);
			if (runs.isEmpty()) {
				destroy();
			}
		}
	}
}
