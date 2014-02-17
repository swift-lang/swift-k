//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 18, 2005
 */
package org.globus.cog.coaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.UDPChannel;

public class UDPService implements Service, Runnable {
	private static final Logger logger = Logger.getLogger(Service.class);

	public static final int BUFFER_SIZE = 2048;

	private ServiceContext context = new ServiceContext(this);
	private Thread serverThread;
	private boolean restricted;
	private Map<InetSocketAddress, CoasterChannel> channels;
	private DatagramSocket socket;
	private RequestManager rm;
	private byte[] recvbuf;
	private boolean shutdownFlag;
	private URI contact;

	public UDPService(RequestManager rm) throws IOException {
		this.rm = rm;
		channels = new HashMap<InetSocketAddress, CoasterChannel>();
	}

	public URI getContact() {
		return contact;
	}

	public void start() throws ChannelException {
		recvbuf = new byte[BUFFER_SIZE];
		try {
			socket = new DatagramSocket();
			InetSocketAddress addr = (InetSocketAddress) socket.getLocalSocketAddress();
			contact = new URI("udp", null, InetAddress.getLocalHost().getHostAddress(),
					addr.getPort(), null, null, null);

		}
		catch (Exception e) {
			throw new ChannelException(e);
		}
		serverThread = new Thread(this);
		serverThread.setName("UDP Service");
		serverThread.start();
	}

	public void run() {
		DatagramPacket dp = new DatagramPacket(recvbuf, recvbuf.length);
		try {
			while (!shutdownFlag) {
				socket.receive(dp);
				InetSocketAddress addr = (InetSocketAddress) dp.getSocketAddress();
				UDPChannel channel;
				synchronized (channels) {
					channel = (UDPChannel) channels.get(addr);
					if (channel == null) {
						ChannelContext cc = new ChannelContext("udp", context);
						channel = new UDPChannel(socket, cc, rm, this, addr);
						channels.put(addr, channel);
						ChannelManager.getManager().registerChannel(
								"udp://" + addr.getAddress().getHostAddress() + ":" + (addr.getPort() + 1), null, channel);
					}
				}
				byte[] buf = new byte[dp.getLength()];
				System.arraycopy(recvbuf, 0, buf, 0, dp.getLength());
				try {
					channel.dataReceived(buf);
				}
				catch (ChannelException e) {
					logger.warn("Channel failed to process incoming message", e);
					synchronized (channels) {
						channels.remove(addr);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return String.valueOf(getContact());
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public ServiceContext getContext() {
		return context;
	}

	public void channelShutDown(UDPChannel channel) {
		synchronized (channels) {
			channels.remove(channel.getRemoteAddress());
		}
	}

	public void irrecoverableChannelError(CoasterChannel channel, Exception e) {
		e.printStackTrace();
	}
}
