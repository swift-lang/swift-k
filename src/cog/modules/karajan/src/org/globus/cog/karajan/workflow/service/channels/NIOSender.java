//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2012
 */
package org.globus.cog.karajan.workflow.service.channels;

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
	private BlockingQueue<AbstractStreamKarajanChannel> add;
	private Map<Channel, BlockingQueue<NIOSendEntry>> registered;
	
	public NIOSender() {
		super("NIO Sender");
		setDaemon(true);
		queues = new HashMap<SelectableChannel, BlockingQueue<NIOSendEntry>>();
		add = new LinkedBlockingQueue<AbstractStreamKarajanChannel>();
		registered = new HashMap<Channel, BlockingQueue<NIOSendEntry>>();
		try {
			selector = Selector.open();
		}
		catch (IOException e) {
			AbstractStreamKarajanChannel.logger.warn("Could not create selector", e);
		}
	}
	
	public void enqueue(int tag, int flags, byte[] data,
			AbstractStreamKarajanChannel channel, SendCallback cb) {
		BlockingQueue<NIOSendEntry> q;
		SelectableChannel c = channel.getNIOChannel();
		
		synchronized(queues) {
			 q = queues.get(c);
			 if (q == null) {
				 q = new LinkedBlockingQueue<NIOSendEntry>();
				 queues.put(c, q);
			 }
			 if (logger.isDebugEnabled()) {
			     logger.debug("Queue for " + channel + " has " + q.size() + " entries");
			 }
			 if (!registered.containsKey(c)) {
				 add.add(channel);
				 registered.put(c, q);
				 selector.wakeup();
			 }
			 q.add(new NIOSendEntry(makeHeader(tag, flags, data), ByteBuffer.wrap(data), channel, cb));
		}
	}
	
	private ByteBuffer makeHeader(int tag, int flags, byte[] data) {
		ByteBuffer bb = ByteBuffer.allocate(AbstractStreamKarajanChannel.HEADER_LEN);
		
		byte[] buf = bb.array();
		Sender.makeHeader(tag, flags, data, bb.array());
				
		return bb;
	}

	public void run() {
		while(true) {
			try {
				int ready = selector.select();
				while (!add.isEmpty()) {
					AbstractStreamKarajanChannel channel = add.poll();
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
					
					NIOSendEntry e = null;
					synchronized(queues) {
						e = q.peek();
						if (e == null) {
							queues.remove(c);
							key.cancel();
							registered.remove(c);
							continue;
						}
					}
					try {
						write(c, e.crt);
						if (logger.isDebugEnabled()) {
						    logger.debug("Sent " + e.crt.position() + " bytes on " + e.channel);
						}
					}
					catch (IOException ee) {
						key.cancel();
						e.channel.handleChannelException(ee);
					}
					if (!e.crt.hasRemaining() && !e.nextBuffer()) {
						if (e.cb != null) {
							e.cb.dataSent();
						}
						q.remove();
					}
					
					i.remove();
				}
			}
			catch (Exception e) {
				logger.error("Exception in send loop", e);
			}
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
        public final AbstractStreamKarajanChannel channel;
        
        public NIOSendEntry(ByteBuffer header, ByteBuffer data, AbstractStreamKarajanChannel channel, SendCallback cb) {
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