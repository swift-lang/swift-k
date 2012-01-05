//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 9, 2005
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;

public class Functions extends FunctionsCollection {

	public static final Arg PA_HOST = new Arg.TypedPositional("host", BoundContact.class, "host");
	public static final Arg PA_TYPE = new Arg.Positional("type");
	public static final Arg PA_PROVIDER = new Arg.Positional("provider");

	static {
		setArguments("task_host_hasservice", new Arg[] { PA_HOST, PA_TYPE, PA_PROVIDER });
	}

	public boolean task_host_hasservice(VariableStack stack) throws ExecutionException {
		BoundContact host = (BoundContact) PA_HOST.getValue(stack);
		String type = TypeUtil.toString(PA_TYPE.getValue(stack));
		String provider = TypeUtil.toString(PA_PROVIDER.getValue(stack));
		if (host.hasService(BoundContact.getServiceType(type), provider)) {
			return true;
		}
		return false;
	}

	static {
		setArguments("task_serviceuri", new Arg[] { PA_HOST, PA_TYPE, PA_PROVIDER });
	}

	public String task_serviceuri(VariableStack stack) throws ExecutionException {
		BoundContact host = (BoundContact) PA_HOST.getValue(stack);
		String type = TypeUtil.toString(PA_TYPE.getValue(stack));
		String provider = TypeUtil.toString(PA_PROVIDER.getValue(stack));
		return host.getService(BoundContact.getServiceType(type), provider).getServiceContact().getContact();
	}

}
