
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.remote;

import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRootContainer;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.generic.Message;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.util.GraphToXML;

public class RemoteContainer extends AbstractRootContainer {
	private static Logger logger = Logger.getLogger(RemoteContainer.class);
	public static RemoteContainer container;
	private String remoteContact;
	private NodeComponent root;
	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private CanvasRenderer renderer;
	private boolean done;
	private boolean connected;

	public RemoteContainer() {
		this(null);
		connected = false;
		done = false;
	}

	public RemoteContainer(String contact) {
		RendererFactory.addClassRenderer(
			GenericNode.class,
			"remote",
			GenericRemoteNodeRenderer.class);
		RendererFactory.addClassRenderer(
			GenericEdge.class,
			"remote",
			GenericRemoteEdgeRenderer.class);
		RendererFactory.addClassRenderer(RootCanvas.class, "remote", RemoteCanvasRenderer.class);
		this.remoteContact = contact;
	}

	public void setRootNode(NodeComponent node) {
		this.root = node;
		if (node.getCanvas() == null) {
			renderer = node.createCanvas().newRenderer("remote");
			logger.info("Supported views are:");
			Iterator i = renderer.getSupportedViews().iterator();
			while (i.hasNext()) {
				logger.info("  " + ((CanvasView) i.next()).getName());
			}
		}
		String prop = (String) node.getPropertyValue("grapheditor.target.remote.contact");
		if (prop != null) {
			logger.info("Remote contact is " + prop);
		}
		else {
			prop = "localhost:9999";
		}
		remoteContact = prop;
	}

	public void run() {
		RemoteContainer.container = this;
		try {
			connect();
			while (!done) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException ie) {

				}
			}
			disconnect();
		}
		catch (Exception e) {
			logger.error(e);
			quit();
		}
	}

	public void dispose() {
		try {
			disconnect();
		}
		catch (IOException e) {
			logger.warn("Cannot disconnect", e);
		}
		super.dispose();
	}

	public void connect() throws UnknownHostException, IOException {
		String[] contact = remoteContact.split(":");
		String host;
		int port;
		if (contact.length == 1) {
			host = contact[0];
			port = 9999;
		}
		else {
			host = contact[0];
			port = Integer.parseInt(contact[1]);
		}
		socket = new Socket(host, port);
		inputStream = new DataInputStream(socket.getInputStream());
		outputStream = new DataOutputStream(socket.getOutputStream());
		connected = true;
	}

	public void disconnect() throws IOException {
		connected = false;
		Message closeMessage = new Message(Message.CMD_CLOSE);
		sendMessageNoReply(closeMessage);
		socket.close();
	}

	public void load(String fileName) {
		super.load(fileName);
		try {
			CharArrayWriter writer = new CharArrayWriter();
			GraphToXML.write(root, writer, 0, true);
			writer.close();
			Message message = new Message(Message.CMD_READ_GRAPH);
			message.addArg(writer.toString());
			sendMessage(message);
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
			logger.error("Error encountered reading " + fileName, e);
		}
	}

	public void updateGraph() {
		try {
			CharArrayWriter writer = new CharArrayWriter();
			GraphToXML.write(root, writer, 0, true);
			writer.close();
			Message message = new Message(Message.CMD_READ_GRAPH);
			message.addArg(writer.toString());
			sendMessage(message);
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
			logger.error("Error encountered while sending graph", e);
		}
	}

	public Message sendMessage(Message message) throws IOException {
		sendMessageNoReply(message);
		Message reply = new Message();
		reply.read(inputStream);
		logger.info(reply.toString());
		return reply;
	}

	public void sendMessageNoReply(Message message) throws IOException {
		logger.info("Sending message " + message);
		message.write(outputStream);
		outputStream.flush();
	}

	public static RemoteContainer getContainer() {
		return container;
	}

	public static void setContainer(RemoteContainer container) {
		RemoteContainer.container = container;
	}

	public void quit() {
		done = true;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean isDone() {
		return done;
	}

	public void updateProperty(String id, String propName, Object object) {
		Message message = new Message(Message.CMD_UPDATE_PROPERTY);
		message.addArg(id);
		message.addArg(propName);
		try {
			message.addArg(GraphToXML.serialize(object));
			sendMessage(message);
		}
		catch (Exception e) {
			logger.error("Unable to send property update command", e);
		}
	}

}
