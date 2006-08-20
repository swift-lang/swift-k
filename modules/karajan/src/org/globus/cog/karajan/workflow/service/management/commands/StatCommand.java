//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 28, 2006
 */
package org.globus.cog.karajan.workflow.service.management.commands;

import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.management.Stat;

public class StatCommand extends Command {
	private Map args;
	private Stat stat;

	public StatCommand() {
		this(null);
	}

	public StatCommand(Map args) {
		super("STAT");
		this.args = args;
	}

	public void send() throws ProtocolException {
		if (args != null) {
			Iterator i = args.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				addOutData(String.valueOf(e.getKey()));
				addOutData(String.valueOf(e.getValue()));
			}
		}
		super.send();
	}

	public void receiveCompleted() {
		super.receiveCompleted();
	}

	public Stat getStat() {
		if (stat == null) {
			stat = (Stat) getInObject(0);
		}
		return stat;
	}
	
	
}
