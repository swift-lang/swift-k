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
 * Created on Jul 28, 2006
 */
package org.globus.cog.coaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServiceContext {
	private Map<String, UserContext> users;
	private Service service;
    private boolean local;
	
	public ServiceContext(Service service) {
		users = new HashMap<String, UserContext>();
		this.service = service;
	}
	
	public void registerUserContext(UserContext uc) {
		synchronized(users) {
			users.put(String.valueOf(uc.getName()), uc);
		}
	}
			
	public Collection<UserContext> getUserContexts() {
		synchronized(users) {
			return new ArrayList<UserContext>(users.values());
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
	
	public void setService(Service service) {
	    this.service = service;
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
