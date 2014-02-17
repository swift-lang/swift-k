//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.handlers.RequestHandler;

public interface CoasterChannel {
	public static final int REPLY_FLAG = 0x00000001;
	public static final int FINAL_FLAG = 0x00000002;
	public static final int ERROR_FLAG = 0x00000004;
	public static final int COMPRESSED_FLAG = 0x00000008;
	public static final int SIGNAL_FLAG = 0x00000010;
	public static final int INITIAL_FLAG = 0x00000020;

	void sendTaggedData(int i, boolean fin, byte[] bytes);

	void sendTaggedData(int i, int flags, byte[] bytes);
	
	void sendTaggedData(int i, boolean fin, byte[] bytes, SendCallback cb);

	void sendTaggedData(int i, int flags, byte[] bytes, SendCallback cb);
	
	void sendTaggedData(int i, int flags, ByteBuffer buf, SendCallback cb);

	void registerCommand(Command command) throws ProtocolException;

	UserContext getUserContext();

	void unregisterHandler(int tag);

	void sendTaggedReply(int i, byte[] buf, boolean fin, boolean err);
	
	void sendTaggedReply(int i, byte[] buf, boolean fin, boolean err, SendCallback cb);
	
	void sendTaggedReply(int i, byte[] buf, int flags);
	
	void sendTaggedReply(int i, byte[] buf, int flags, SendCallback cb);
	
	void sendTaggedReply(int id, ByteBuffer buf, boolean fin, boolean err, SendCallback cb);
	
	void sendTaggedReply(int id, ByteBuffer buf, int flags, SendCallback cb);

	void registerHandler(RequestHandler handler, int tag);

	RequestManager getRequestManager();

	int incUsageCount();

	int decUsageCount();
	
	void start() throws ChannelException;

	void shutdown();

	void close();

	void setLocalShutdown();
	
	ChannelContext getChannelContext();

	void setChannelContext(ChannelContext context);

	boolean isOffline();
	
	boolean isStarted();
	
	void unregisterCommand(Command cmd);

	int incLongTermUsageCount();

	int decLongTermUsageCount();

	/*
	 * Provided for the sole purpose of being able to deterministically decide a
	 * priority for things that are otherwise symmetrical.
	 */
	boolean isClient();
	
	URI getCallbackURI() throws Exception;

	void setRequestManager(RequestManager requestManager);

	void flush() throws IOException;
	
	SelectableChannel getNIOChannel();
	
	boolean handleChannelException(Exception e);		
}
