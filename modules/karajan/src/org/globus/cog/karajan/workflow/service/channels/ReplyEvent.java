//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 23, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventClass;
import org.globus.cog.karajan.workflow.events.Priority;

public class ReplyEvent extends Event {
	private final int tag;
	private final byte[] data;
	private final boolean err, fin;
	
	public ReplyEvent(int tag, byte[] data, boolean fin, boolean err) {
		super(EventClass.GENERIC_EVENT, null, Priority.HIGH);
		this.tag = tag;
		this.data = data;
		this.err = err;
		this.fin = fin;
	}

	public byte[] getData() {
		return data;
	}

	public boolean getErr() {
		return err;
	}

	public boolean getFin() {
		return fin;
	}

	public int getTag() {
		return tag;
	}

		
}
