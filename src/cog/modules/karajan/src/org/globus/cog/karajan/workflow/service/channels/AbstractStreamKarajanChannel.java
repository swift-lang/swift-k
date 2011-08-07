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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.RemoteConfiguration;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.commands.ChannelConfigurationCommand;

public abstract class AbstractStreamKarajanChannel extends AbstractKarajanChannel implements
		Purgeable {
	public static final Logger logger = Logger.getLogger(AbstractStreamKarajanChannel.class);

	public static final int STATE_IDLE = 0;
	public static final int STATE_RECEIVING_DATA = 1;

	public static final int HEADER_LEN = 12;

	private InputStream inputStream;
	private OutputStream outputStream;
	private URI contact;
	private final byte[] rhdr;
	private byte[] data;
	private int dataPointer;
	private int state, tag, flags, len;

	protected AbstractStreamKarajanChannel(RequestManager requestManager,
			ChannelContext channelContext, boolean client) {
		super(requestManager, channelContext, client);
		rhdr = new byte[HEADER_LEN];
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

	protected abstract void reconnect() throws ChannelException;

	protected synchronized boolean handleChannelException(Exception e) {
		logger.info("Channel config: " + getChannelContext().getConfiguration());
		if (!ChannelManager.getManager().handleChannelException(this, e)) {
			close();
			return false;
		}
		else {
		    return true;
		}
	}

	protected void configure() throws Exception {
		URI callbackURI = null;
		ChannelContext sc = getChannelContext();
		if (sc.getConfiguration().hasOption(RemoteConfiguration.CALLBACK)) {
			callbackURI = getCallbackURI();
		}
		// String remoteID = sc.getChannelID().getRemoteID();

		ChannelConfigurationCommand ccc = new ChannelConfigurationCommand(sc.getConfiguration(),
				callbackURI);
		ccc.execute(this);
		logger.info("Channel configured");
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data, SendCallback cb) {
		getSender(this).enqueue(tag, flags, data, this, cb);
	}

	private static long savail, cnt;

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
				if (len > 1048576) {
					logger.warn("Big len: " + len + " (tag: " + tag + ", flags: " + flags + ")");
					data = new byte[1024];
					inputStream.read(data);
					logger.warn("data: " + ppByteBuf(data));
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
				boolean fin = (flags & FINAL_FLAG) != 0;
				boolean error = (flags & ERROR_FLAG) != 0;
				byte[] tdata = data;
				// don't hold reference from the channel to the data
				data = null;
				if ((flags & REPLY_FLAG) != 0) {
					// reply
					handleReply(tag, fin, error, len, tdata);
				}
				else {
					// request
					handleRequest(tag, fin, error, len, tdata);
				}
				data = null;
			}
		}
		return any;
	}

	public void purge(KarajanChannel channel) throws IOException {
		getSender(this).purge(this, channel);
	}

	protected void register() {
		getMultiplexer(FAST).register(this);
	}

	protected void unregister() {
		getMultiplexer(FAST).unregister(this);
	}

	public void flush() throws IOException {
		outputStream.flush();
	}

	private static Map<Class<? extends KarajanChannel>, Sender> sender;

	private static synchronized Sender getSender(KarajanChannel channel) {
		if (sender == null) {
			sender = 
				new HashMap<Class<? extends KarajanChannel>, Sender>();
		}

		Sender s = sender.get(channel.getClass());
		if (s == null) {
			sender.put(channel.getClass(), s = new Sender());
			s.start();
		}
		return s;
	}

	private static class SendEntry {
		public final int tag, flags;
		public final byte[] data;
		public final AbstractStreamKarajanChannel channel;
		public final SendCallback cb;

		public SendEntry(int tag, int flags, byte[] data, AbstractStreamKarajanChannel channel, SendCallback cb) {
			this.tag = tag;
			this.flags = flags;
			this.data = data;
			this.channel = channel;
			this.cb = cb;
		}
	}

	private static class Sender extends Thread {
		private final LinkedList<SendEntry> queue;
		private final byte[] shdr;

		public Sender() {
			super("Sender");
			queue = new LinkedList<SendEntry>();
			setDaemon(true);
			shdr = new byte[HEADER_LEN];
		}

		public synchronized void enqueue(int tag, int flags, byte[] data,
				AbstractStreamKarajanChannel channel, SendCallback cb) {
			queue.addLast(new SendEntry(tag, flags, data, channel, cb));
			notifyAll();
		}

		public void run() {
			long last = System.currentTimeMillis();
			try {
				SendEntry e;
				while (true) {
					long now = System.currentTimeMillis();
					synchronized (this) {
						while (queue.isEmpty()) {
							wait();
						}
						e = queue.removeFirst();
						if (now - last > 10000) {
							logger.info("Sender " + System.identityHashCode(this) + " queue size: "
									+ queue.size());
							last = now;
						}
					}
					try {
						send(e.tag, e.flags, e.data, e.channel.getOutputStream());
						if (e.cb != null) {
							e.cb.dataSent();
						}
					}
					catch (IOException ex) {
						logger.info("Channel IOException", ex);
						try {
							if (e.channel.handleChannelException(ex)) {
								synchronized (this) {
									queue.addFirst(e);
								}
							}
						}
						catch (Exception exx) {
						    logger.warn("Channel threw exception while handling channel exception", exx);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
						try {
							e.channel.getChannelContext().getRegisteredCommand(e.tag).errorReceived(
									null, ex);
						}
						catch (Exception exx) {
							logger.warn(exx);
						}
					}
				}
			}
			catch (InterruptedException e) {
				// exit
			}
		}

		public void purge(KarajanChannel source, KarajanChannel channel) {
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

		private void send(int tag, int flags, byte[] data, OutputStream os) throws IOException {
			pack(shdr, 0, tag);
			pack(shdr, 4, flags);
			pack(shdr, 8, data.length);
			synchronized (os) {
				os.write(shdr);
				os.write(data);
				if ((flags & FINAL_FLAG) != 0) {
					os.flush();
				}
			}
		}
	}

	private static final int MUX_COUNT = 2;
	private static Multiplexer[] multiplexer;
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

	protected static class Multiplexer extends Thread {
		public static final Logger logger = Logger.getLogger(Multiplexer.class);

		private Set<KarajanChannel> channels;
		private List<KarajanChannel> remove, add;
		private boolean terminated;
		private int id;

		@SuppressWarnings("unchecked")
		public Multiplexer(int id) {
			super("Channel multiplexer " + id);
			this.id = id;
			setDaemon(true);
			channels = new HashSet<KarajanChannel>();
			remove = Collections.synchronizedList(new ArrayList<KarajanChannel>());
			add = Collections.synchronizedList(new ArrayList<KarajanChannel>());
		}

		public synchronized void register(AbstractStreamKarajanChannel channel) {
			add.add(channel);
			if (logger.isInfoEnabled()) {
				logger.info("(" + id + ") Scheduling " + channel + " for addition");
			}
			if (terminated) {
				logger.warn("Trying to add a channel to a stopped multiplexer");
			}
		}

		public void run() {
			logger.info("Multiplexer " + id + " started");
			boolean any;
			long last = System.currentTimeMillis();
			try {
				while (true) {
					any = false;
					if (this == multiplexer[0]) {
						savail = 0;
						cnt = 0;
					}
					Iterator<KarajanChannel> i = channels.iterator();
					while (i.hasNext()) {
						AbstractStreamKarajanChannel channel = 
							(AbstractStreamKarajanChannel) i.next();
						if (channel.isClosed()) {
							if (logger.isInfoEnabled()) {
								logger.info("Channel is closed. Removing.");
							}
							i.remove();
						}
						try {
							any |= channel.step();
						}
						catch (Exception e) {
						    logger.info("Exception in channel step", e);
							try {
								shutdown(channel, e);
							}
							catch (Exception ee) {
								logger.warn("Failed to shut down channel", ee);
							}
						}
					}
					synchronized (this) {
						i = remove.iterator();
						while (i.hasNext()) {
							Object r = i.next();
							channels.remove(r);
						}
						i = add.iterator();
						while (i.hasNext()) {
							KarajanChannel a = i.next();
							channels.add(a);
						}
						remove.clear();
						add.clear();
					}
					if (this == multiplexer[0]) {
						long now = System.currentTimeMillis();
						if (now - last > 10000) {
							if (cnt > 0) {
								logger.info("Avg stream buf: " + (savail / cnt));
							}
							else {
								logger.info("No streams");
							}
							last = now;
						}
					}
					if (!any) {
						Thread.sleep(20);
					}
				}
			}
			catch (Exception e) {
				logger.warn("Exception in channel multiplexer", e);
			}
			catch (Error e) {
				logger.error("Error in multiplexer", e);
				e.printStackTrace();
				System.exit(10);
			}
			finally {
				logger.info("Multiplexer finished");
				terminated = true;
			}
		}

		public synchronized void unregister(AbstractStreamKarajanChannel channel) {
			if (logger.isInfoEnabled()) {
				logger.info("Unregistering channel " + channel);
			}
			remove.add(channel);
		}

		private void shutdown(AbstractStreamKarajanChannel channel, Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Channel exception caught", e);
			}
			channel.handleChannelException(e);
			synchronized (this) {
				if (logger.isInfoEnabled()) {
					logger.info("Removing faulty channel " + channel);
				}
				remove.add(channel);
			}
		}
	}
}
