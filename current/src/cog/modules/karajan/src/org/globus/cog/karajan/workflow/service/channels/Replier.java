//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 23, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventListener;

public class Replier implements EventListener {
	private static final Logger logger = Logger.getLogger(Replier.class);

	private final KarajanChannel channel;

	public Replier(KarajanChannel channel) {
		this.channel = channel;
	}

	public void event(Event event) throws ExecutionException {
		if (event instanceof ReplyEvent) {
			ReplyEvent r = (ReplyEvent) event;
			int tag = r.getTag();
			byte[] data = r.getData();
			boolean fin = r.getFin();
			boolean err = r.getErr();
			if (logger.isDebugEnabled()) {
				logger.debug(this + "REPL>: tag = " + tag + ", fin = " + fin + ", datalen = "
						+ data.length + ", data = " + AbstractKarajanChannel.ppByteBuf(data));
			}
			int flags = KarajanChannel.REPLY_FLAG;
			if (fin) {
				flags |= KarajanChannel.FINAL_FLAG;
			}
			if (err) {
				flags |= KarajanChannel.ERROR_FLAG;
			}
			channel.sendTaggedData(tag, flags, data);
		}
		else {
			throw new RuntimeException("Invalid event type: " + event.getClass());
		}
	}

	public String toString() {
		return "Replier(" + channel + ")";
	}

}
