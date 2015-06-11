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
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

class NIOSender extends Thread {
    public static final Logger logger = Logger.getLogger(NIOSender.class);
    
	private Map<SelectableChannel, BlockingQueue<NIOSendEntry>> queues;
	private Selector selector;
	private BlockingQueue<AbstractStreamCoasterChannel> add;
	private Map<Channel, BlockingQueue<NIOSendEntry>> registered;
	
	public NIOSender() {
		super("NIO Sender");
		setDaemon(true);
		queues = new HashMap<SelectableChannel, BlockingQueue<NIOSendEntry>>();
		add = new LinkedBlockingQueue<AbstractStreamCoasterChannel>();
		registered = new HashMap<Channel, BlockingQueue<NIOSendEntry>>();
		try {
			selector = Selector.open();
		}
		catch (IOException e) {
			AbstractStreamCoasterChannel.logger.warn("Could not create selector", e);
		}
	}
	
	public void enqueue(int tag, int flags, byte[] data,
			AbstractStreamCoasterChannel channel, SendCallback cb) {
	    if (data == null) {
	        throw new NullPointerException();
	    }
		BlockingQueue<NIOSendEntry> q;
		SelectableChannel c = channel.getNIOChannel();
		
		synchronized(queues) {
			 q = queues.get(c);
			 if (q == null) {
				 q = new LinkedBlockingQueue<NIOSendEntry>();
				 queues.put(c, q);
			 }
			 if (logger.isTraceEnabled()) {
			     logger.trace("Queue for " + channel + " has " + q.size() + " entries");
			 }
			 if (!registered.containsKey(c)) {
				 add.add(channel);
				 registered.put(c, q);
				 selector.wakeup();
			 }
			 q.add(new NIOSendEntry(makeHeader(tag, flags, data), ByteBuffer.wrap(data), channel, cb));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("send channel: " + channel + ", tag: " + tag + ", flags: " + flags + ", len: " + data.length);
		}
	}
	
	private ByteBuffer makeHeader(int tag, int flags, byte[] data) {
		ByteBuffer bb = ByteBuffer.allocate(AbstractStreamCoasterChannel.HEADER_LEN);
		
		byte[] buf = bb.array();
		Sender.makeHeader(tag, flags, data, bb.array());

		return bb;
	}

	public void run() {
		while(true) {
			try {
				int ready = selector.select();
				while (!add.isEmpty()) {
					AbstractStreamCoasterChannel channel = add.poll();
					SelectableChannel c = channel.getNIOChannel();
					BlockingQueue<NIOSendEntry> q;
					synchronized(queues) {
						q = registered.get(c);
					}
					try {
						c.register(selector, SelectionKey.OP_WRITE, q);
					}
					catch (ClosedChannelException e) {
						channel.handleChannelException(e);
					}
				}
				if (ready == 0) {
					continue;
				}
				
				if (logger.isDebugEnabled()) {
				    logger.debug(ready + " ready channels");
				}
				
				Iterator<SelectionKey> i = selector.selectedKeys().iterator();
				while (i.hasNext()) {
					SelectionKey key = i.next();
					
					WritableByteChannel c = (WritableByteChannel) key.channel();
					@SuppressWarnings("unchecked")
					BlockingQueue<NIOSendEntry> q = (BlockingQueue<NIOSendEntry>) key.attachment();
					
					sendAllEntries(q, c, key);
							
					i.remove();
				}
			}
			catch (Exception e) {
				logger.error("Exception in send loop", e);
			}
		}
	}
	
	private void sendAllEntries(BlockingQueue<NIOSendEntry> q, WritableByteChannel c, SelectionKey key) {
	    NIOSendEntry e = null;
	    while (true) {
	        // get one entry from queue
            synchronized(queues) {
                e = q.peek();
                if (e == null) {
                    queues.remove(c);
                    key.cancel();
                    registered.remove(c);
                    return;
                }
            }
            if (sendAllBuffers(e, c, key)) {
                notifySender(e);
                q.remove();
            }
            else {
                return;
            }
	    }
	}
	
    private void notifySender(NIOSendEntry e) {
        if (e.cb != null) {
            try {
                e.cb.dataSent();
            }
            catch (Exception ee) {
                logger.warn("Callback threw exception", ee);
            }
        }
    }

    private boolean sendAllBuffers(NIOSendEntry e, WritableByteChannel c, SelectionKey key) {
        while (true) {
            try {
                write(c, e.crt);
                if (logger.isTraceEnabled()) {
                    logger.trace("Sent " + e.crt.position() + " bytes on " + e.channel);
                }
            }
            catch (IOException ee) {
                key.cancel();
                e.channel.handleChannelException(ee);
                return false;
            }
         
            // not all bytes were sent; assume TCP buffer full and yield
            if (e.crt.hasRemaining()) {
                return false;
            }
            
            // there are no more buffers in this entry, return to allow processing others
            if (!e.nextBuffer()) {
                return true;
            }
            
            // there is another buffer, so loop to send it
        }
    }

    private void write(WritableByteChannel c, ByteBuffer buf) throws IOException {
		if (AbstractTCPChannel.logPerformanceData) {
			PerformanceDiagnosticOutputStream.bytesWritten(c.write(buf));
		}
		else {
			c.write(buf);
		}
	}

	class NIOSendEntry {
        public ByteBuffer data;
        public ByteBuffer crt;
        public final SendCallback cb;
        public final AbstractStreamCoasterChannel channel;
        
        public NIOSendEntry(ByteBuffer header, ByteBuffer data, AbstractStreamCoasterChannel channel, SendCallback cb) {
            this.data = data;
            crt = header;
            this.cb = cb;
            this.channel = channel;
        }
    
        public boolean nextBuffer() {
            if (crt != data) {
                crt = data;
                return true;
            }
            else {
                data = null;
                return false;
            }
        }
	}
}