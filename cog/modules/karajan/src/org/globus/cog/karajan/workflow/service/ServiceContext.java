//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 28, 2006
 */
package org.globus.cog.karajan.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.ietf.jgss.GSSName;

public class ServiceContext {
	private Map users;
	private Service service;
    private boolean local;
	
	public ServiceContext(Service service) {
		users = new HashMap();
		this.service = service;
	}
	
	public void registerUserContext(UserContext uc) {
		synchronized(users) {
			users.put(String.valueOf(uc.getName()), uc);
		}
	}
	
	public UserContext getUserContext(GSSName name, ChannelContext channelContext) {
		return getUserContext(name.toString(), channelContext);
	}
	
	public UserContext getUserContext(String name, ChannelContext channelContext) {
		//TODO this doesn't make much sense
		synchronized(users) {
			String sname = String.valueOf(name);
			UserContext uc = (UserContext) users.get(sname);
			if (uc == null) {
				uc = new UserContext(name, channelContext);
				users.put(sname, uc);
			}
			return uc;
		}
	}
	
	public Collection getUserContexts() {
		synchronized(users) {
			return new ArrayList(users.values());
		}
	}
	
	public void unregisterUserContext(UserContext uc) {
		unregisterUserContext(String.valueOf(uc.getName()));
	}
	
	public void unregisterUserContext(String username) {
		synchronized(users) {
			users.remove(username);
		}
	}

	public Service getService() {
		return service;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isRestricted() {
		return service.isRestricted();
	}	
}
