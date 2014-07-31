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
 * Created on Aug 1, 2005
 */
package org.globus.cog.coaster.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RemoteConfiguration;
import org.globus.cog.coaster.RequestReply;
import org.globus.cog.coaster.Service;
import org.globus.cog.coaster.ServiceContext;
import org.globus.cog.coaster.UserContext;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.handlers.RequestHandler;
import org.ietf.jgss.GSSCredential;

public class ChannelContext {
	private static final Logger logger = Logger.getLogger(ChannelContext.class);

	private Map<String,Object> attributes;
	private boolean initialized;
	private RemoteConfiguration.Entry configuration;
	private ChannelID channelID;
	private String remoteContact;
	private final String name;
	private UserContext userContext;
	private int cmdseq;
	private TagTable<Command> activeSenders;
	private TagTable<RequestHandler> activeReceivers;
	private ServiceContext serviceContext;
	private int reconnectionAttempts;
	private long lastHeartBeat;
	
	private List<ChannelListener> listeners;

	public ChannelContext(String name) {
		this(name, new ServiceContext(null));
	}
	
	public ChannelContext(String name, ServiceContext sc) {
		activeSenders = new TagTable<Command>();
		activeReceivers = new TagTable<RequestHandler>();

		channelID = new ChannelID();
		this.serviceContext = sc;
		this.name = name;
	}
	
	public ChannelContext(String name, Service service) {
		this(name, service.getContext());
		channelID.setClient(false);
	}

	public synchronized UserContext getUserContext() {
		return userContext;
	}
		
	public synchronized void setUserContext(UserContext userContext) {
		this.userContext = userContext;
	}
	
	public synchronized UserContext newUserContext(String name) throws ChannelException {
		if (userContext != null && userContext.getName() != null) {
			try {
				if (!userContext.getName().equals(name)) {
					throw new ChannelException("Invalid identity. Expected "
							+ userContext.getName() + " but got " + name);
				}
			}
			catch (ChannelException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ChannelException(e);
			}
		}
		userContext = serviceContext.getUserContext(name, null, this);
		return userContext;
	}
	
	public synchronized UserContext newUserContext(GSSCredential cred) throws ChannelException {
		String name = UserContext.getName(cred);
		if (userContext != null) {
			try {
				if (!userContext.getName().equals(name)) {
					throw new ChannelException("Invalid identity. Expected "
							+ userContext.getName() + " but got " + name);
				}
			}
			catch (ChannelException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ChannelException(e);
			}
		}
		userContext = serviceContext.getUserContext(name, cred, this);
		return userContext;
	}

	public synchronized void initialize() {
	}

	public RemoteConfiguration.Entry getConfiguration() {
		return configuration;
	}

	public void setConfiguration(RemoteConfiguration.Entry configuration) {
		this.configuration = configuration;
	}

	public String getRemoteContact() {
		return remoteContact;
	}

	public void setRemoteContact(String remoteContact) {
		this.remoteContact = remoteContact;
	}

	public String getName() {
		return name;
	}

	public ChannelID getChannelID() {
		return channelID;
	}
	
	public synchronized int nextCmdSeq() {
		cmdseq = cmdseq + 1;
		while (activeSenders.containsKey(cmdseq) || activeReceivers.containsKey(cmdseq)) {
			cmdseq = cmdseq + 1;
		}
		return cmdseq;
	}

	public void registerCommand(Command cmd) throws ProtocolException {
		if (cmd.getId() == RequestReply.NOID) {
			cmd.setId(nextCmdSeq());
			synchronized(activeSenders) {
				activeSenders.put(cmd.getId(), cmd);
			}
		}
		else {
			throw new ProtocolException("Command already registered with id " + cmd.getId());
		}
	}
	
	public Collection<Command> getActiveCommands() {
		List<Command> l = new ArrayList<Command>();
		synchronized(activeSenders) {
		    l.addAll(activeSenders.values());
		}
		return l;
	}
	
	public Collection<RequestHandler> getActiveHandlers() {
	    List<RequestHandler> l = new ArrayList<RequestHandler>();
	    synchronized(activeReceivers) {
	    	l.addAll(activeReceivers.values());
	    }
	    return l;
	}

	public void unregisterCommand(Command cmd) {
		Object removed;
		synchronized(activeSenders) {
			removed = activeSenders.remove(cmd.getId());
		}
		if (removed == null) {
			logger.warn("Attempted to unregister unregistered command with id " + cmd.getId());
		}
		else {
			cmd.setId(RequestReply.NOID);
		}
	}

	public void registerHandler(RequestHandler handler, int tag) {
		synchronized(activeReceivers) {
			activeReceivers.put(tag, handler);
		}
	}

	public void unregisterHandler(int tag) {
		Object removed;
		synchronized(activeReceivers) {
			removed = activeReceivers.remove(tag);
		}
		if (removed == null) {
			logger.warn("Attempted to unregister unregistered handler with id " + tag);
		}
	}

	public Command getRegisteredCommand(int id) {
		synchronized(activeSenders) {
			return activeSenders.get(id);
		}
	}

	public RequestHandler getRegisteredHandler(int id) {
		synchronized(activeReceivers) {
			return activeReceivers.get(id);
		}
	}

	public void notifyRegisteredCommandsAndHandlers(Exception e) {
		if (logger.isInfoEnabled()) {
			logger.info("Notifying commands and handlers about exception", e);
		}
		notifyListeners(activeReceivers, e);
		notifyListeners(activeSenders, e);
	}

	private void notifyListeners(TagTable<? extends RequestReply> map, Exception t) {
		Collection<RequestReply> l = new ArrayList<RequestReply>();
		synchronized(map) {
			l.addAll(map.values());
		}
		for (RequestReply r : l) {
			if (logger.isInfoEnabled()) {
				logger.info("=> " + r);
			}
			r.errorReceived(null, t);
		}
	}

	public void reexecute(final Command command) {
		//TODO
	}
	
	public Service getService() {
		return serviceContext.getService();
	}
	
	public void setService(Service service) {
		serviceContext.setService(service);
	}
	
	public ServiceContext getServiceContext() {
		return serviceContext;
	}
	
	public Object getData(String name) {
	    return getAttribute(name);
	}
	
	public synchronized void setAttribute(String name, Object o) {
	    if (attributes == null) {
	        attributes = new HashMap<String, Object>();
	    }
	    attributes.put(name, o);
	}
	
	public synchronized Object getAttribute(String name) {
	    if (attributes == null) {
	        return null;
	    }
	    else {
	        return attributes.get(name);
	    }
	}

	public int getReconnectionAttempts() {
		return reconnectionAttempts;
	}

	public void setReconnectionAttempts(int reconnectionAttempts) {
		this.reconnectionAttempts = reconnectionAttempts;
	}

	public long getLastHeartBeat() {
		return lastHeartBeat;
	}

	public void setLastHeartBeat(long lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}
	
	public String toString() {
		return name;
	}
	
	public synchronized void addChannelListener(ChannelListener l) {
	    if (listeners == null) {
	        listeners = new LinkedList<ChannelListener>();
	    }
	    listeners.add(l);
	}
	
	public synchronized void removeChannelListener(ChannelListener l) {
	    if (listeners != null) {
	        listeners.remove(l);
	    }
	}

	public synchronized void channelShutDown(Exception e) {
		notifyRegisteredCommandsAndHandlers(e);
	    if (listeners != null) {
	        for (ChannelListener l : listeners) {
	            try {
	            	l.channelShutDown(e);
	            }
	            catch (Exception ee) {
	                logger.warn("Failed to notify listener of channel shutdown", ee);
	            }
	        }
	    }
	}
	
}
