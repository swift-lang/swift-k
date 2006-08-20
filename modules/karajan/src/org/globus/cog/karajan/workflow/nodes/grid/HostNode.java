// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class HostNode extends AbstractFunction {
	public static final Integer DEFAULT_CPUS = new Integer(1);

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_CPUS = new Arg.Optional("cpus", DEFAULT_CPUS);
	public static final Arg.Channel A_PROPERTIES = new Arg.Channel("properties");

	static {
		setArguments(HostNode.class, new Arg[] { A_NAME, A_CPUS, A_PROPERTIES, Arg.VARGS });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		BoundContact contact = new BoundContact();
		String host = TypeUtil.toString(A_NAME.getValue(stack));
		contact.setHost(host);
		contact.setCpus(TypeUtil.toInt(A_CPUS.getValue(stack)));
		Iterator i = Arg.VARGS.get(stack).iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (!(o instanceof Service)) {
				throw new ExecutionException("Unexpected argument " + o);
			}
			Service s = (Service) o;
			if (s.getServiceContact() == null) {
				s.setServiceContact(new ServiceContactImpl(host));
			}
			contact.addService(s);
		}
		Map properties = contact.getProperties();
		if (properties == null) {
			properties = new HashMap();
			contact.setProperties(properties);
		}
		i = A_PROPERTIES.get(stack).iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			properties.put(e.getKey(), e.getValue());
		}
		return contact;
	}
}