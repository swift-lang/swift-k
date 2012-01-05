//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 1, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.ietf.jgss.GSSCredential;

public class UserContext {

	private String name;
	private GSSCredential credential;
	private final Map instances;
	private final ChannelContext channelContext;

	public UserContext(String name, ChannelContext channelContext) {
		this.name = name;
		instances = new HashMap();
		this.channelContext = channelContext;
		if (channelContext == null) {
			throw new IllegalArgumentException("channelContext cannot be null");
		}
		if (channelContext.getServiceContext() != null) {
			channelContext.getServiceContext().registerUserContext(this);
		}
	}

	public GSSCredential getCredential() {
		return credential;
	}

	public void setCredential(GSSCredential credential) {
		this.credential = credential;
	}

	public String getName() {
		return name;
	}

	public InstanceContext newInstanceContext() {
		return new InstanceContext(this);
	}

	public InstanceContext newInstanceContext(String remoteID) {
		synchronized (instances) {
			if (instances.containsKey(remoteID)) {
				throw new IllegalStateException("Instance already exists in user context: "
						+ remoteID);
			}
			InstanceContext ic = new InstanceContext(this);
			ic.setServerID(remoteID);
			instances.put(ic.getID(), ic);
			return ic;
		}
	}

	public void registerInstanceContext(InstanceContext ic) {
		synchronized (instances) {
			instances.put(ic.getID(), ic);
		}
	}

	public InstanceContext getInstanceContext(String id) {
		synchronized (instances) {
			return (InstanceContext) instances.get(id);
		}
	}

	/**
	 * Returns the channel context of the channel that created this user context
	 */
	public ChannelContext getChannelContext() {
		return channelContext;
	}

	public int instanceContextCount() {
		synchronized (instances) {
			return instances.size();
		}
	}

	public void removeInstanceContext(InstanceContext ic) {
		synchronized (instances) {
			instances.remove(ic.getID());
			if (instances.size() == 0 && !(channelContext.getServiceContext() == null)) {
				channelContext.getServiceContext().unregisterUserContext(this);
			}
		}
	}

	public Map getInstances() {
		synchronized (instances) {
			return new HashMap(instances);
		}
	}
}
