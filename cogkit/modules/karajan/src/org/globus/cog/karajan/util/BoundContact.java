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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 18, 2003
 */
package org.globus.cog.karajan.util;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * This class sub-classes {@link Contact} and adds concrete
 * contact information, such as a {@link #getHost host} and
 * a set of {@link #getServices services}.
 * 
 * @author Mihael Hategan
 *
 */
public class BoundContact extends Contact {
	private Map<TypeProviderPair,Service> services;

	private String name;

	private int cpus;

	private int activeTasks;
	
	private Map<String, Object> properties;

	public static final BoundContact LOCALHOST = new Localhost();

	public BoundContact() {
		super();
		services = new HashMap<TypeProviderPair, Service>();
		cpus = 1;
	}

	public BoundContact(String name) {
		this();
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addService(Service sc) {
		services.put(new TypeProviderPair(sc.getType(), sc.getProvider()), sc);
		TypeProviderPair first = new TypeProviderPair(sc.getType(), null);
		if (!services.containsKey(first)) {
			services.put(first, sc);
		}
		if (getName() == null) {
			if (sc.getServiceContact().getHost() != null) {
				setName(sc.getServiceContact().getHost());
			}
		}
	}

	public void removeService(int type, String provider) {
		services.remove(new TypeProviderPair(type, provider));
	}

	public boolean hasService(int type, String provider) {
		return services.containsKey(new TypeProviderPair(type, provider));
	}

	public boolean hasService(TaskHandlerWrapper handler) {
		return hasService(getServiceType(handler.getType()), handler.getProvider());
	}

	public Service getService(int type, String provider) {
		return services.get(new TypeProviderPair(type, provider));
	}

	public Service getService(TaskHandlerWrapper handler) {
		return getService(getServiceType(handler.getType()), handler.getProvider());
	}
	
	public Service findService(int type) {
	    Service found = null;
	    
	    for (Map.Entry<TypeProviderPair, Service> e : services.entrySet()) {
	        if (e.getKey().type == type) {
	            if (found != null) {
	                throw new IllegalStateException("More than one service of type " + 
	                    type + " exists for host '" + this.getName() + "'");
	            }
	            found = e.getValue();
	        }
	    }
	    return found;
	}

	public static int getServiceType(int handlerType) {
		if (handlerType == TaskHandler.EXECUTION) {
			return Service.EXECUTION;
		}
		else if (handlerType == TaskHandler.FILE_TRANSFER) {
			return Service.FILE_OPERATION;
		}
		else if (handlerType == TaskHandler.FILE_OPERATION) {
			return Service.FILE_OPERATION;
		}
		else {
			throw new RuntimeException("Unknown handler type: " + handlerType);
		}
	}

	public static int getServiceType(String type) {
		if (type == null) {
			return Service.EXECUTION;
		}
		if (type.equalsIgnoreCase("execution") || type.equalsIgnoreCase("job-submission")) {
			return Service.EXECUTION;
		}
		if (type.equalsIgnoreCase("file")) {
			return Service.FILE_OPERATION;
		}
		return Service.EXECUTION;
	}

	public Map<TypeProviderPair, Service> getServices() {
		return services;
	}

	public int getCpus() {
		return this.cpus;
	}

	public void setCpus(int cpus) {
		this.cpus = cpus;
	}

	public boolean isVirtual() {
		return false;
	}

	public int getActiveTasks() {
		return activeTasks;
	}

	public void setActiveTasks(int activeTasks) {
		this.activeTasks = activeTasks;
	}

	public String toString() {
		return name;
	}

	public boolean equals(Object obj) {
		if (obj instanceof BoundContact) {
			BoundContact bc = (BoundContact) obj;
			if (name == null) {
				return bc.getName() == null;
			}
			return name.equals(bc.getName());
		}
		return false;
	}

	public int hashCode() {
		if (name != null) {
			return name.hashCode();
		}
		else {
			return System.identityHashCode(this);
		}
	}

	public static class TypeProviderPair {
		public final int type;

		public final String provider;

		public TypeProviderPair(int type, String provider) {
			this.type = type;
			this.provider = provider;
		}

		public boolean equals(Object obj) {
			if (obj instanceof TypeProviderPair) {
				TypeProviderPair other = (TypeProviderPair) obj;
				
				if (type != other.type) {
					return false;
				}

				if (provider == null) {
					return other.provider == null;
				}
				return provider.equals(other.provider);
			}
			return false;
		}

		public int hashCode() {
			return (provider == null ? 0 : provider.hashCode());
		}

		public String toString() {
			return type + ":" + provider;
		}
	}

	public static class Localhost extends BoundContact {
		public static final ServiceContact LOCALHOST = new ServiceContactImpl("localhost");

		private final Service fileService = new ServiceImpl("local", new ServiceContactImpl("localhost"),
				null);
		private final Service transferService = new ServiceImpl("local", new ServiceContactImpl(
				"localhost"), null);
		private final Service executionService = new ServiceImpl("local", new ServiceContactImpl(
				"localhost"), null);

		public Localhost() {
			fileService.setType(Service.FILE_OPERATION);
			transferService.setType(Service.FILE_TRANSFER);
			executionService.setType(Service.EXECUTION);
			addService(fileService);
			addService(transferService);
			addService(executionService);
			//TODO A better way to avoid this being equal to a host who happens
			//to have the same name should be implemented
			setName("_localhost");
		}

		public Service getService(int type, String provider) {
			if (type == Service.FILE_OPERATION || type == Service.FILE_TRANSFER
					|| type == Service.EXECUTION) {
				return new ServiceImpl(provider, LOCALHOST, null);
			}
			else {
				return super.getService(type, provider);
			}
		}
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String,Object> properties) {
		this.properties = properties;
	}

	public boolean hasProperty(String name) {
		return properties != null && properties.containsKey(name);
	}
	
	public Object getProperty(String name) {
		if (properties == null) {
			return null;
		}
		else {
			return properties.get(name);
		}
	}
	
	public void setProperty(String name, Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(name, value);
	}	
}