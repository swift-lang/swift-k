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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2012
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Adler32;

import org.apache.log4j.Logger;


class Sender extends Thread {
    public static final Logger logger = Logger.getLogger(Sender.class);
    
    public static final boolean PAYLOAD_CHECKSUM = false;
    
	private final BlockingQueue<SendEntry> queue;
	private final byte[] shdr;
	private final String name;

	public Sender(String name) {
		super("Sender " + name);
		this.name = name;
		queue = new LinkedBlockingQueue<SendEntry>();
		setDaemon(true);
		shdr = new byte[AbstractStreamCoasterChannel.HEADER_LEN];
	}

	public void enqueue(int tag, int flags, byte[] data,
			AbstractStreamCoasterChannel channel, SendCallback cb) {
		try {
			queue.put(new SendEntry(tag, flags, data, channel, cb));
		}
		catch (InterruptedException e) {
			logger.warn("Interrupted", e);
		}
	}

	public void run() {
		long last = System.currentTimeMillis();
		try {
			SendEntry e;
			while (true) {
				long now = System.currentTimeMillis();
				
				e = queue.take();
				if (now - last > 10000) {
					logger.info("Sender " + name + " queue size: " + queue.size());
					last = now;
				}
				
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("send channel: " + e.channel + ", tag: " + e.tag + ", flags: " + e.flags + ", len: " + e.data.length);
					}
					send(e.tag, e.flags, e.data, e.channel.getOutputStream());
					if (e.cb != null) {
						e.cb.dataSent();
					}
				}
				catch (IOException ex) {
					logger.info("Channel IOException caught sending tag " + e.tag + " on " + e.channel, ex);
					try {
						e.channel.handleChannelException(ex);
					}
					catch (Exception exx) {
					    logger.warn("Channel threw exception while handling channel exception", exx);
					}
				}
				catch (Exception ex) {
					logger.warn("Caught exception while sending data", ex);
					try {
						e.channel.getRegisteredCommand(e.tag).errorReceived(null, ex);
					}
					catch (Exception exx) {
						logger.warn("Exception", exx);
					}
				}
			}
		}
		catch (InterruptedException e) {
			// exit
		}
	}

	public void purge(CoasterChannel source, CoasterChannel channel) {
		SendEntry e;
		synchronized (this) {
			Iterator<SendEntry> i = queue.iterator();
			while (i.hasNext()) {
				e = i.next();
				if (e.channel == source) {
					channel.sendTaggedData(e.tag, e.flags, e.data);
					i.remove();
				}
			}
		}
	}
	
	public static void makeHeader(int tag, int flags, byte[] data, byte[] hdr) {
	    AbstractStreamCoasterChannel.pack(hdr, 0, tag);
        AbstractStreamCoasterChannel.pack(hdr, 4, flags);
        AbstractStreamCoasterChannel.pack(hdr, 8, data.length);
        AbstractStreamCoasterChannel.pack(hdr, 12, tag ^ flags ^ data.length);
        if (PAYLOAD_CHECKSUM) {
        	Adler32 csum = new Adler32();
        	csum.update(data);
        	AbstractStreamCoasterChannel.pack(hdr, 16, (int) csum.getValue());
        }
        AbstractStreamCoasterChannel.pack(hdr, 16, 0);
	}

	private void send(int tag, int flags, byte[] data, OutputStream os) throws IOException {
		makeHeader(tag, flags, data, shdr);
		synchronized (os) {
			os.write(shdr);
			os.write(data);
			if ((flags & AbstractStreamCoasterChannel.FINAL_FLAG) != 0) {
				os.flush();
			}
		}
	}
	
	private static class SendEntry {
		public final int tag, flags;
		public final byte[] data;
		public final AbstractStreamCoasterChannel channel;
		public final SendCallback cb;

        public SendEntry(int tag, int flags, byte[] data, AbstractStreamCoasterChannel channel, SendCallback cb) {
            this.tag = tag;
            this.flags = flags;
            this.data = data;
            this.channel = channel;
            this.cb = cb;
        }
	}
}
