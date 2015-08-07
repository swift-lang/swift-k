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
 * Created on Jul 21, 2006
 */
package org.globus.cog.coaster.channels;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;

public abstract class AbstractStreamCoasterChannel extends AbstractCoasterChannel implements
		Purgeable {
	public static final Logger logger = Logger.getLogger(AbstractStreamCoasterChannel.class);

	public static final int STATE_IDLE = 0;
	public static final int STATE_RECEIVING_DATA = 1;

	public static final int HEADER_LEN = 20;

	private InputStream inputStream;
	private OutputStream outputStream;
	private URI contact;
	private final byte[] rhdr = new byte[HEADER_LEN];
	private final ByteBuffer bhdr = ByteBuffer.wrap(rhdr);
	private byte[] data;
	private ByteBuffer bdata;
	private int dataPointer;
	private int state, tag, flags, len, hcsum, csum;

	protected AbstractStreamCoasterChannel(RequestManager requestManager, UserContext userContext, boolean client) {
		super(requestManager, userContext, client);
	}
	
	protected InputStream getInputStream() {
		return inputStream;
	}

	protected void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	protected OutputStream getOutputStream() {
		return outputStream;
	}

	protected void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public URI getContact() {
		return contact;
	}

	public void setContact(URI contact) {
		this.contact = contact;
	}

	public void sendTaggedData(int tag, int flags, byte[] data, SendCallback cb) {
		if (getNIOChannel() != null) {
			getNIOSender(this).enqueue(tag, flags, data, this, cb);
		}
		else {
			getSender(this).enqueue(tag, flags, data, this, cb);
		}
	}

	static long cnt;

	static long savail;

	protected boolean step() throws IOException {
		int avail = inputStream.available();
		savail += avail;
		cnt++;
		if (avail == 0) {
			return false;
		}
		// we can only rely on GsiInputStream.available() returning 0 if nothing
		// is available
		// see https://bugzilla.mcs.anl.gov/globus/show_bug.cgi?id=6747
		boolean any = false;
		if (state == STATE_IDLE) {
			dataPointer = readFromStream(inputStream, rhdr, dataPointer);
			if (dataPointer == HEADER_LEN) {
				tag = unpack(rhdr, 0);
				flags = unpack(rhdr, 4);
				len = unpack(rhdr, 8);
				hcsum = unpack(rhdr, 12);
				if ((tag ^ flags ^ len) != hcsum) {
					throw new IOException("Header checksum failed. Computed checksum: " + 
							Integer.toHexString(tag ^ flags ^ len) + 
							", checksum: " + Integer.toHexString(hcsum));
				}
				csum = unpack(rhdr, 16);
				if (logger.isDebugEnabled()) {
                    logger.debug("recv channel: "+ this + ", tag: " + tag + ", flags: " + flags + ", len: " + len);
                }
				if (len > 1048576) {
					logger.warn("Big len: " + len + " (tag: " + tag + ", flags: " + flags + ")");
					data = new byte[1024];
					inputStream.read(data);
					logger.warn("data: " + ppByteBuf(data));
					return true;
				}
				data = new byte[len];
				dataPointer = 0;
				state = STATE_RECEIVING_DATA;
				avail = inputStream.available();
				any = true;
			}
		}
		if (state == STATE_RECEIVING_DATA) {
			while (avail > 0 && dataPointer < len) {
				any = true;
				dataPointer = readFromStream(inputStream, data, dataPointer);
				avail = inputStream.available();
			}
			if (dataPointer == len) {
				dataPointer = 0;
				state = STATE_IDLE;
				
				if (csum != 0) {
					Adler32 c = new Adler32();
					c.update(data);
					
					if (((int) c.getValue()) != csum) {
						logger.warn("Data checksum failed. Compute checksum: " + 
								Integer.toHexString((int) c.getValue()) + ", checksum: " + Integer.toHexString(csum));
					}
				}
				byte[] tdata = data;
				// don't hold reference from the channel to the data
				data = null;
				if (flagIsSet(flags, REPLY_FLAG)) {
					// reply
					handleReply(tag, flags, len, tdata);
				}
				else {
					// request
					handleRequest(tag, flags, len, tdata);
				}
				data = null;
			}
		}
		return any;
	}
	
	protected void stepNIO() throws IOException {
		ReadableByteChannel channel = (ReadableByteChannel) getNIOChannel();
		if (state == STATE_IDLE) {
			readFromChannel(channel, bhdr);
			if (!bhdr.hasRemaining()) {
				tag = unpack(rhdr, 0);
				flags = unpack(rhdr, 4);
				len = unpack(rhdr, 8);
				hcsum = unpack(rhdr, 12);
				if (logger.isDebugEnabled()) {
					logger.debug("NIOrecv channel: "+ this + ", tag: " + tag + ", flags: " + flags + ", len: " + len);
				}
				if ((tag ^ flags ^ len) != hcsum) {
					logger.warn("(NIO) Header checksum failed. Computed checksum: " + 
							Integer.toHexString(tag ^ flags ^ len) + 
							", checksum: " + Integer.toHexString(hcsum));
					logger.warn("Tag: " + tag + ", flags: " + flags + ", len: " + len + ", data: " + ppByteBuf(rhdr));
					channel.close();
					throw new IOException("Header checksum failed");
				}
				csum = unpack(rhdr, 16);
				if (len > 1048576) {
					logger.warn("Big len: " + len + " (tag: " + tag + ", flags: " + flags + ")");
					bdata = ByteBuffer.wrap(data = new byte[1024]);
					readFromChannel(channel, bdata);
					logger.warn("data: " + ppByteBuf(data));
					return;
				}
				bdata = ByteBuffer.wrap(data = new byte[len]);
				state = STATE_RECEIVING_DATA;
				bhdr.rewind();
			}
		}
		if (state == STATE_RECEIVING_DATA) {
			readFromChannel(channel, bdata);
			
			if (!bdata.hasRemaining()) {
				state = STATE_IDLE;
				
				if (csum != 0) {
					Adler32 c = new Adler32();
					c.update(data);
					
					if (((int) c.getValue()) != csum) {
						logger.warn("Data checksum failed. Compute checksum: " + 
								Integer.toHexString((int) c.getValue()) + ", checksum: " + Integer.toHexString(csum));
					}
				}
				byte[] tdata = data;
				// don't hold reference from the channel to the data
				data = null; bdata = null;
				if (flagIsSet(flags, REPLY_FLAG)) {
					// reply
					handleReply(tag, flags, len, tdata);
				}
				else {
					// request
					handleRequest(tag, flags, len, tdata);
				}
			}
		}
	}
	
	protected void readFromChannel(ReadableByteChannel c, ByteBuffer buf) throws IOException {
		if (AbstractTCPChannel.logPerformanceData) {
			PerformanceDiagnosticInputStream.bytesRead(c.read(buf));
		}
		else {
			int read = c.read(buf);
			if (read == -1) {
			    throw new EOFException();
			}
		}
	}

	public void purge(CoasterChannel channel) throws IOException {
		getSender(this).purge(this, channel);
	}

	protected void register() {
		if  (getNIOChannel() != null) {
			getNIOMultiplexer().register(this);
		}
		else {
			getMultiplexer(FAST).register(this);
		}
	}

	protected void unregister() {
		if  (getNIOChannel() != null) {
			getNIOMultiplexer().unregister(this);
		}
		else {
			getMultiplexer(FAST).unregister(this);
		}
	}

	public void flush() throws IOException {
	    purge(this);
		outputStream.flush();
	}

	private static Map<Class<? extends CoasterChannel>, Sender> sender;

	private static synchronized Sender getSender(CoasterChannel channel) {
		if (sender == null) {
			sender = 
				new HashMap<Class<? extends CoasterChannel>, Sender>();
		}

		Sender s = sender.get(channel.getClass());
		if (s == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using threaded sender for " + channel);
			}
			sender.put(channel.getClass(), s = new Sender(channel.getClass().getSimpleName()));
			s.start();
		}
		return s;
	}
	
	private static NIOSender nioSender;
	
	private static synchronized NIOSender getNIOSender(CoasterChannel channel) {
		if (nioSender == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using NIO sender for " + channel);
			}
			nioSender = new NIOSender();
			nioSender.start();
		}

		return nioSender;
	}

	private static final int MUX_COUNT = 2;
	static Multiplexer[] multiplexer;
	public static final int FAST = 0;
	public static final int SLOW = 1;

	public static synchronized Multiplexer getMultiplexer(int n) {
		if (multiplexer == null) {
			multiplexer = new Multiplexer[MUX_COUNT];
			for (int i = 0; i < MUX_COUNT; i++) {
				multiplexer[i] = new Multiplexer(i);
				multiplexer[i].start();
			}
		}
		return multiplexer[n];
	}

	private static NIOMultiplexer nioMultiplexer;
	
	private static synchronized NIOMultiplexer getNIOMultiplexer() {
		if (nioMultiplexer == null) {
			nioMultiplexer = new NIOMultiplexer();
			nioMultiplexer.start();
		}
		
		return nioMultiplexer;
	}
}
