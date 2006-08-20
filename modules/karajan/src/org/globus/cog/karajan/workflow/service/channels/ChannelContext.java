//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 1, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestReply;
import org.globus.cog.karajan.workflow.service.Service;
import org.globus.cog.karajan.workflow.service.ServiceContext;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;

public class ChannelContext {
	private static final Logger logger = Logger.getLogger(ChannelContext.class);

	private Map data;
	private boolean initialized;
	private RemoteConfiguration.Entry configuration;
	private ChannelID channelID;
	private String remoteContact;
	private UserContext userContext;
	private int cmdseq;
	private TagTable activeSenders;
	private TagTable activeReceivers;
	private Map reexecutionSet;
	private static Timer timer;
	private ServiceContext serviceContext;
	private GSSCredential cred;

	public ChannelContext() {
		this(new ServiceContext(null));
	}
	public ChannelContext(ServiceContext sc) {
	    data = new HashMap();
		activeSenders = new TagTable();
		activeReceivers = new TagTable();
		reexecutionSet = new Hashtable();
		channelID = new ChannelID();
		this.serviceContext = sc;
	}
	
	public ChannelContext(Service service) {
		this(service.getContext());
		channelID.setClient(false);
	}

	public synchronized UserContext getUserContext() {
		return userContext;
	}
	
	public synchronized UserContext newUserContext(GSSName name) throws ChannelException {
		return newUserContext(name.toString());
	}

	public synchronized UserContext newUserContext(String name) throws ChannelException {
		if (userContext != null) {
			try {
				if (!userContext.getName().equals(name)) {
					throw new ChannelException("Invalid identity. Expected "
							+ userContext.getName() + " but got " + name);
				}
			}
			catch (Exception e) {
				throw new ChannelException(e);
			}
		}
		userContext = serviceContext.getUserContext(name, this);
		return userContext;
	}

	public synchronized void initialize() {
		if (!initialized) {
			EventBus.initialize();
		}
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
			activeSenders.put(cmd.getId(), cmd);
		}
		else {
			throw new ProtocolException("Command already registered with id " + cmd.getId());
		}
	}

	public void unregisterCommand(Command cmd) {
		Object removed;
		removed = activeSenders.remove(cmd.getId());
		if (removed == null) {
			logger.warn("Attemted to unregister unregistered command with id " + cmd.getId());
		}
		else {
			cmd.setId(RequestReply.NOID);
		}
	}

	public void registerHandler(RequestHandler handler, int tag) {
		activeReceivers.put(tag, handler);
	}

	public void unregisterHandler(int tag) {
		Object removed;
		removed = activeReceivers.remove(tag);
		if (removed == null) {
			logger.warn("Attemted to unregister unregistered handler with id " + tag);
		}
	}

	public Command getRegisteredCommand(int id) {
		return (Command) activeSenders.get(id);
	}

	public RequestHandler getRegisteredHandler(int id) {
		return (RequestHandler) activeReceivers.get(id);
	}

	protected void notifyRegisteredListeners(Exception e) {
		notifyListeners(activeReceivers, e);
		notifyListeners(activeSenders, e);
	}

	private void notifyListeners(TagTable map, Exception t) {
		Iterator i = map.values().iterator();
		while (i.hasNext()) {
			((RequestReply) i.next()).channelClosed();
		}
	}

	public Timer getTimer() {
		synchronized (ChannelContext.class) {
			if (timer == null) {
				timer = new Timer();
			}
			return timer;
		}
	}

	public void reexecute(final Command command) {
		//todo
	}
	
	public Service getService() {
		return serviceContext.getService();
	}
	
	public ServiceContext getServiceContext() {
		return serviceContext;
	}

	public void setCredential(GSSCredential cred) {
		this.cred = cred;
	}
	
	public GSSCredential getCredential() {
		return cred;
	}
	
	public Object getData(String name) {
	    synchronized(data) {
	        return data.get(name);
	    }
	}
	
	public void addData(String name, Object o) {
	    synchronized(data) {
	        data.put(name, o);
	    }
	}
}
