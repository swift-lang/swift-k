// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class ServiceNode extends AbstractFunction {
	private static final Logger logger = Logger.getLogger(ServiceNode.class);

	public static final Arg A_TYPE = new Arg.Positional("type");
	public static final Arg A_PROVIDER = new Arg.Positional("provider");
	public static final Arg A_URI = new Arg.Optional("uri", null);
	public static final Arg A_URL = new Arg.Optional("url", null);
	public static final Arg A_SECURITY_CONTEXT = new Arg.Optional("securityContext", null);
	public static final Arg A_JOB_MANAGER = new Arg.Optional("jobManager");
	public static final Arg A_PROJECT = new Arg.Optional("project");

	static {
		setArguments(ServiceNode.class, new Arg[] { A_TYPE, A_PROVIDER, A_URI, A_URL,
				A_SECURITY_CONTEXT, A_JOB_MANAGER, A_PROJECT });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Service service = null;
		String type = TypeUtil.toString(A_TYPE.getValue(stack));
		int itype = 0;
		if (type.equals("execution") || type.equals("job-submission")) {
			itype = Service.JOB_SUBMISSION;
			service = new ExecutionServiceImpl();
			if (A_JOB_MANAGER.isPresent(stack)) {
				((ExecutionService) service).setJobManager(TypeUtil.toString(A_JOB_MANAGER.getValue(stack)));
			}
		}
		else if (type.equals("file-transfer")) {
			itype = Service.FILE_TRANSFER;
		}
		else if (type.equals("file-operation") || type.equals("file")) {
			itype = Service.FILE_OPERATION;
		}
		else {
			throw new ExecutionException("Invalid service type: " + type);
		}

		if (service == null) {
			service = new ServiceImpl();
		}
		String provider = TypeUtil.toString(A_PROVIDER.getValue(stack));
		service.setProvider(provider);

		if (A_PROJECT.isPresent(stack)) {
			service.setAttribute("project", TypeUtil.toString(A_PROJECT.getValue(stack)));
		}

		SecurityContext sc = (SecurityContext) A_SECURITY_CONTEXT.getValue(stack);
		if (sc == null) {
			String scName = "#" + provider + "#defaultSecurityContext";
			try {
				sc = (SecurityContext) stack.getVar(scName);
			}
			catch (VariableNotFoundException e) {
				logger.debug("No default security context defined for provider " + provider);
				if (sc == null) {
					try {
						sc = AbstractionFactory.newSecurityContext(provider);
						stack.setGlobal(scName, sc);
					}
					catch (Exception e1) {
						throw new ExecutionException(
								"No security context can be found or created for service (provider "
										+ provider + "): " + e1.getMessage(), e1);
					}
				}
			}
		}
		service.setSecurityContext(sc);
		service.setType(itype);
		String uri = null;
		uri = TypeUtil.toString(A_URI.getValue(stack));
		if (uri == null) {
			uri = TypeUtil.toString(A_URL.getValue(stack));
		}
		if (uri != null) {
			service.setServiceContact(new ServiceContactImpl(uri));
		}
		return service;
	}
}