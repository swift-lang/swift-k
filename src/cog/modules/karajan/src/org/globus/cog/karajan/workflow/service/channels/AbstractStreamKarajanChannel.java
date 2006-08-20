//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.NoSuchHandlerException;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public abstract class AbstractStreamKarajanChannel extends AbstractKarajanChannel {
	public static final Logger logger = Logger.getLogger(AbstractStreamKarajanChannel.class); 
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private String endpoint;
	private final ByteBuffer header;
	private final byte[] bheader;
	
	protected AbstractStreamKarajanChannel(RequestManager requestManager, ChannelContext channelContext) {
		super(requestManager, channelContext);
		bheader = new byte[12];
		header = ByteBuffer.wrap(bheader);
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

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data) {
		header.clear();
		header.putInt(tag);
		header.putInt(flags);
		header.putInt(data.length);
		try {
			outputStream.write(bheader);
			outputStream.write(data);
			outputStream.flush();
		}
		catch (IOException e) {
			throw new ChannelIOException(e);
		}
	}


	protected void mainLoop() throws IOException {
		ChannelContext context = getChannelContext();
		ByteBuffer header = ByteBuffer.allocate(12);
		while (!isClosed()) {
				header.clear();
				while (header.remaining() > 0) {
					readFromStream(inputStream, header);
				}
				header.rewind();
				IntBuffer iheader = header.asIntBuffer();
				int tag = iheader.get();
				int flags = iheader.get();
				int len = iheader.get();
				byte[] data = new byte[len];
				int crt = 0;
				while (crt < len) {
					crt = readFromStream(inputStream, data, crt);
				}
				boolean fin = (flags & FINAL_FLAG) != 0;
				boolean error = (flags & ERROR_FLAG) != 0;
				if ((flags & REPLY_FLAG) != 0) {
					// reply
					if (logger.isDebugEnabled()) {
						logger.debug(this + "REPL<: tag = " + tag + ", fin = " + fin + ", err = "
								+ error + ", datalen = " + len + ", data = " + ppByteBuf(data));
					}
					Command cmd = context.getRegisteredCommand(tag);
	
					if (cmd != null) {
						cmd.replyReceived(data);
						if (fin) {
							if (error) {
								cmd.errorReceived();
							}
							else {
								cmd.receiveCompleted();
							}
							unregisterCommand(cmd);
						}
					}
					else {
						logger.warn(endpoint + "Recieved reply to unregistered sender. Tag: " + tag);
					}
				}
				else {
					// request
					if (logger.isDebugEnabled()) {
						logger.debug(this + "REQ<: tag = " + tag + ", fin = " + fin + ", err = "
								+ error + ", datalen = " + len + ", data = " + ppByteBuf(data));
					}
					RequestHandler handler = context.getRegisteredHandler(tag);
					try {
						if (handler != null) {
							handler.register(this);
							handler.dataReceived(data);
							if (fin) {
								try {
									handler.receiveCompleted();
								}
								catch (ChannelIOException e) {
									throw e;
								}
								catch (Exception e) {
									if (!handler.isReplySent()) {
										handler.sendError(e.toString(), e);
									}
								}
								catch (Error e) {
									if (!handler.isReplySent()) {
										handler.sendError(e.toString(), e);
									}
									throw e;
								}
								finally {
									unregisterHandler(tag);
								}
							}
						}
						else {
							try {
								handler = getRequestManager().handleInitialRequest(data);
								handler.setId(tag);
								registerHandler(handler, tag);
								if (fin) {
									try {
										if (error) {
											// TODO
										}
										else {
											handler.receiveCompleted();
										}
									}
									catch (ChannelIOException e) {
										throw e;
									}
									catch (Exception e) {
										if (!handler.isReplySent()) {
											handler.sendError(e.toString(), e);
										}
									}
									finally {
										unregisterHandler(tag);
									}
								}
							}
							catch (NoSuchHandlerException e) {
								logger.warn(endpoint + "Could not handle request", e);
							}
						}
					}
					catch (ProtocolException e) {
						unregisterHandler(tag);
						logger.warn(e);
					}
				}
			}
	}

}
