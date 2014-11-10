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
 * Created on Jul 19, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.Collection;

import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.Service;
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
	
	
	String getID();

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
	
	void start() throws ChannelException;

	void shutdown();

	void close();
	
	boolean isClosed();

	void setLocalShutdown();
		
	boolean isStarted();
	
	void unregisterCommand(Command cmd);

	/*
	 * Provided for the sole purpose of being able to deterministically decide a
	 * priority for things that are otherwise symmetrical.
	 */
	boolean isClient();
	
	void setRequestManager(RequestManager requestManager);

	void flush() throws IOException;
	
	SelectableChannel getNIOChannel();
	
	void handleChannelException(Exception e);
		
    Collection<Command> getActiveCommands();
    
    Collection<RequestHandler> getActiveHandlers();

    Command getRegisteredCommand(int id);

    RequestHandler getRegisteredHandler(int id);
    
    Service getService();
    
    void setService(Service service);
    
    long getLastHeartBeat();

    void setLastHeartBeat(long lastHeartBeat);
        
    void addListener(ChannelListener l);
    
    void removeListener(ChannelListener l);
    
    void setName(String name);
    
    String getName();
    
    boolean supportsOption(ChannelOptions.Type type, Object value);
    
    void setOption(ChannelOptions.Type type, Object value);
}
