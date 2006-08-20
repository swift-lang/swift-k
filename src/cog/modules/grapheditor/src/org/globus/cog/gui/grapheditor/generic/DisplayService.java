
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.generic;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DisplayService extends Thread {
	private static Logger logger = Logger.getLogger(DisplayService.class);

	public static final String VERSION = "0.3";

	private boolean done;

	private ServerSocket socket;

	private Socket mysocket;

	private CommandListener cl;

	private boolean listener;

	public DisplayService(Socket sock, CommandListener cl) {
		listener = false;
		this.mysocket = sock;
		this.cl = cl;
		start();
	}

	public DisplayService(int port, CommandListener cl) {
		this.cl = cl;
		listener = true;
		try {
			socket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
		}
		catch (Exception e) {
			throw new RuntimeException("Could not start listening service: " + e.getMessage());
		}
		start();
	}

	public void run() {
		if (listener) {
			while (!done) {
				try {
					Socket sock = socket.accept();
					new DisplayService(sock, cl);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			loop(mysocket);
		}
	}

	private void loop(Socket sock) {
		try {
			boolean done = false;
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			while (!done) {
				try {
					Message msg = new Message();
					msg.read(dis);
					logger.debug("Received: " + msg);
					if (msg.getCommand() == Message.CMD_CLOSE) {
						done = true;
					}
					else if (msg.getCommand() == Message.CMD_VERSION) {
						Message reply = new Message(Message.REPLY_OK);
						reply.addArg("Service version is " + VERSION);
						logger.debug("Sending: " + reply);
						reply.write(dos);
						dos.flush();
					}
					else {
						Message reply = cl.processCommand(msg);
						logger.debug("Sending: " + reply);
						reply.write(dos);
						dos.flush();
					}
				}
				catch (IOException ioe) {
					done = true;
				}
				catch (Exception e) {
					e.printStackTrace();
					Message message = new Message(Message.REPLY_UNKNOWN_ERROR);
					message.addArg(e.getMessage());
					message.write(dos);
					dos.flush();
				}
			}
			sock.close();
		}
		catch (Exception ee) {
			throw new RuntimeException("Error setting-up socket: " + ee.getMessage());
		}
	}

}
