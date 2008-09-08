//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 19, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.net.URI;

import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public interface KarajanChannel {
	public static final int REPLY_FLAG = 0x00000001;
	public static final int FINAL_FLAG = 0x00000002;
	public static final int ERROR_FLAG = 0x00000004;

	void sendTaggedData(int i, boolean fin, byte[] bytes);

	void sendTaggedData(int i, int flags, byte[] bytes);

	void registerCommand(Command command) throws ProtocolException;

	UserContext getUserContext();

	void unregisterHandler(int tag);

	void sendTaggedReply(int i, byte[] buf, boolean fin, boolean errorFlag);

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
}
