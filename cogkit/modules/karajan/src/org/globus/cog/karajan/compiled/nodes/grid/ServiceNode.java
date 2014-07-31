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

package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;

public class ServiceNode extends AbstractSingleValuedFunction {
	private static final Logger logger = Logger.getLogger(ServiceNode.class);
	
	private static final Map<String, Integer> stypes;
	
	static {
		stypes = new HashMap<String,Integer>();
		stypes.put(AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER, new Integer(
				Service.EXECUTION));
		stypes.put(AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER, new Integer(
				Service.FILE_TRANSFER));
		stypes.put(AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER, new Integer(
				Service.FILE_OPERATION));
	}
	
	public static int abstractionToServiceType(String type) {
		try {
			return stypes.get(type).intValue();
		}
		catch (NullPointerException e) {
			throw new ExecutionException("Invalid abstraction handler type: " + type);
		}
	}
	
	public static int karajanToServiceType(String type) {
		return abstractionToServiceType(TaskHandlerNode.karajanToAbstractionType(type));
	}


	private ArgRef<String> type;
	private ArgRef<String> provider;
	private ArgRef<String> URI;
	private ArgRef<String> URL;
	private ArgRef<SecurityContext> securityContext;
	private ArgRef<String> jobManager;
	private ArgRef<String> project;
		
	@Override
	protected Param[] getParams() {
		return params("type", "provider", optional("URI", null), optional("URL", null), 
				optional("securityContext", null), optional("jobManager", null), optional("project", null));
	}

	public Object function(Stack stack) {
		Service service = null;
		String type = this.type.getValue(stack);
		int itype = karajanToServiceType(type);
		if (itype == Service.EXECUTION) {
			itype = Service.EXECUTION;
			service = new ExecutionServiceImpl();
			String jobManager = this.jobManager.getValue(stack);
			if (jobManager != null) {
				((ExecutionService) service).setJobManager(jobManager);
			}
		}

		if (service == null) {
			service = new ServiceImpl();
		}
		String provider = this.provider.getValue(stack);
		service.setProvider(provider);

		String project = this.project.getValue(stack);
		if (project != null) {
			service.setAttribute("project", project);
		}

		service.setType(itype);
		String uri = this.URI.getValue(stack);
		if (uri == null) {
			uri = this.URL.getValue(stack);
		}
		ServiceContact contact = null;
		if (uri != null) {
			service.setServiceContact(contact = new ServiceContactImpl(uri));
		}
		
		SecurityContext sc = securityContext.getValue(stack);
		if (sc == null) {
			String scName = "#" + provider + "#defaultSecurityContext";
			try {
				sc = AbstractionFactory.newSecurityContext(provider, contact);
			}
			catch (Exception e1) {
				throw new ExecutionException(
						"No security context can be found or created for service (provider "
								+ provider + "): " + e1.getMessage(), e1);
			}
		}
		service.setSecurityContext(sc);
		
		return service;
	}
}