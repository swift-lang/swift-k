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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

class NIOMultiplexer extends Thread {
	public static final Logger logger = Logger.getLogger(NIOMultiplexer.class);
	
	private Selector selector;
	private BlockingQueue<AbstractStreamCoasterChannel> add;
	
	public NIOMultiplexer() {
		super("NIO Multiplexer");
		setDaemon(true);
		add = new LinkedBlockingQueue<AbstractStreamCoasterChannel>();
		try {
			selector = Selector.open();
		}
		catch (IOException e) {
			AbstractStreamCoasterChannel.logger.error("Failed to open selector", e);
			throw new RuntimeException(e);
		}
	}

	public void register(AbstractStreamCoasterChannel channel) {
		if (logger.isDebugEnabled()) {
			logger.debug("Registering " + channel);
		}
		add.add(channel);
		selector.wakeup();
	}

	public void unregister(AbstractStreamCoasterChannel channel) {
		channel.getNIOChannel().keyFor(selector).cancel();
	}
	
	public void run() {
		try {
			loop();
		}
		catch (Exception e) {
			logger.error("Error in multiplexer loop", e);
			e.printStackTrace();
			System.exit(10);
		}
		catch (Error e) {
			logger.error("Error in NIO multiplexer", e);
			e.printStackTrace();
			System.exit(10);
		}
	}

	private void loop() throws IOException {
		while(true) {
			int ready = selector.select();
			
			while (!add.isEmpty()) {
				AbstractStreamCoasterChannel channel = add.poll();
				try {
					channel.getNIOChannel().register(selector, SelectionKey.OP_READ, channel);
				}
				catch (ClosedChannelException e) {
					channel.handleChannelException(e);
				}
			}
			if (ready == 0) {
				continue;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug(ready + " channels ready for reading");
			}
		
			Iterator<SelectionKey> i = selector.selectedKeys().iterator();
			while (i.hasNext()) {
				SelectionKey key = i.next();
				ReadableByteChannel c = (ReadableByteChannel) key.channel();
				
				try {
					((AbstractStreamCoasterChannel) key.attachment()).stepNIO();
				}
				catch (IOException e) {
					((AbstractStreamCoasterChannel) key.attachment()).handleChannelException(e);
					key.cancel();
				}
				i.remove();
			}
		}
	}
}