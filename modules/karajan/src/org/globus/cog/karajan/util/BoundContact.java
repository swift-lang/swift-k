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

public class BoundContact extends Contact {
	private Map services;

	private String host;

	private int cpus;

	private int activeTasks;
	
	private Map properties;

	public static final BoundContact LOCALHOST = new Localhost();

	public BoundContact() {
		super();
		services = new HashMap();
		cpus = 1;
	}

	public BoundContact(String host) {
		this();
		this.host = host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void addService(Service sc) {
		services.put(new TypeProviderPair(sc.getType(), sc.getProvider()), sc);
		TypeProviderPair first = new TypeProviderPair(sc.getType(), null);
		if (!services.containsKey(first)) {
			services.put(first, sc);
		}
		if (getHost() == null) {
			if (sc.getServiceContact().getHost() != null) {
				setHost(sc.getServiceContact().getHost());
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
		return (Service) services.get(new TypeProviderPair(type, provider));
	}

	public Service getService(TaskHandlerWrapper handler) {
		return getService(getServiceType(handler.getType()), handler.getProvider());
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

	public Map getServices() {
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
		return host;
	}

	public boolean equals(Object obj) {
		if (obj instanceof BoundContact) {
			BoundContact bc = (BoundContact) obj;
			if (host == null) {
				return bc.getHost() == null;
			}
			return host.equals(bc.getHost());
		}
		return false;
	}

	public int hashCode() {
		if (host != null) {
			return host.hashCode();
		}
		else {
			return System.identityHashCode(this);
		}
	}

	private static class TypeProviderPair {
		public int type;

		public String provider;

		public TypeProviderPair(int type, String provider) {
			this.type = type;
			this.provider = provider == null ? null : provider.toLowerCase();
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
			setHost("localhost");
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

	public Map getProperties() {
		return properties;
	}

	public void setProperties(Map properties) {
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
}