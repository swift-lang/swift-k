//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 27, 2006
 */
package org.globus.cog.karajan.scheduler;

import java.util.Map;

import org.globus.cog.abstraction.impl.file.gridftp.GridFTPConstants;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.workflow.nodes.grid.BDP;

public class TCPBufferSizeTransformer implements TaskTransformer {

	public void transformTask(Task task, Contact[] contacts, Service[] services) {
		Object o = task.getAttribute(GridFTPConstants.ATTR_TCP_BUFFER_SIZE);
		if (o instanceof Map) {
			task.setAttribute(GridFTPConstants.ATTR_TCP_BUFFER_SIZE, BDP.getBufferSize((Map) o,
					services[0].getServiceContact().getContact(),
					services[1].getServiceContact().getContact()));
		}
	}
}
