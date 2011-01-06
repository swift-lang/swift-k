//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 28, 2006
 */
package org.globus.cog.karajan.workflow.service.management.handlers;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.service.InstanceContext;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.ServiceContext;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;
import org.globus.cog.karajan.workflow.service.management.Stat;

public class StatHandler extends RequestHandler {

	public void requestComplete() throws ProtocolException {
		ServiceContext sc = getChannel().getChannelContext().getServiceContext();
		Collection c = sc.getUserContexts();
		Stat stat = new Stat();
		Iterator i = c.iterator();
		while (i.hasNext()) {
			UserContext uc = (UserContext) i.next();
			String name = uc.getName();
			Stat.User user = new Stat.User(name == null ? "unknown" : name.toString());
			stat.addUser(user);
			Map instances = uc.getInstances();
			Iterator j = instances.entrySet().iterator();
			while (j.hasNext()) {
				Map.Entry e = (Map.Entry) j.next();
				InstanceContext ic = (InstanceContext) e.getValue();
				Stat.Instance instance = new Stat.Instance(ic.getID(), ic.getName());
				user.addInstance(instance);
				Collection runs = ic.getExecutionContexts();
				Iterator k = runs.iterator();
				while (k.hasNext()) {
					ExecutionContext ec = (ExecutionContext) k.next();
					String status;
					if (ec.done()) {
						if (ec.isFailed()) {
							status = "Failed";
						}
						else {
							status = "Completed";
						}
					}
					else {
						status = "Running";
					}
					Calendar st = Calendar.getInstance();
					st.setTimeInMillis(ec.getStartTime());
					String sst = st.getTime().toString();
					String set;
					if (ec.getEndTime() != 0) {
						st.setTimeInMillis(ec.getEndTime());
						set = st.getTime().toString();
					}
					else {
						set = "N/A";
					}
					Stat.Run run = new Stat.Run(String.valueOf(ec.getId()), status, sst, set);
					instance.addRun(run);
				}
			}
		}
		addOutObject(stat);
		sendReply();
	}
}
