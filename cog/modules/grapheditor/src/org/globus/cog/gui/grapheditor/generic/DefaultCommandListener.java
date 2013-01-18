
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Jan 28, 2004
 *
 */
package org.globus.cog.gui.grapheditor.generic;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.XMLToGraph;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

public class DefaultCommandListener implements CommandListener{
	private RootNode root;
	
	public DefaultCommandListener(RootNode root) {
		this.root = root;
	}
	
	public Message processCommand(Message msg) {
		if (msg.getCommand() == Message.CMD_READ_GRAPH) {
			Reader r = new StringReader((String) msg.getArgs().get(0));
			root.getCanvas().setEventsActive(false);
			root.getCanvas().getGraph().clear();
			XMLToGraph.read(root, r, null);
			root.getCanvas().setEventsActive(true);
			root.getCanvas().invalidate();
			return new Message(Message.REPLY_OK);
		}
		else if (msg.getCommand() == Message.CMD_UPDATE_PROPERTY) {
			String nodeid = (String) msg.getArgs().get(0);
			String pname = (String) msg.getArgs().get(1);
			String pvalue = (String) msg.getArgs().get(2);
			NodeComponent nc = findNode(root, nodeid);
			if (nc == null) {
				return new Message(Message.REPLY_INVALID_NODEID);
			}
			if (!nc.hasProperty(pname)) {
				return new Message(Message.REPLY_INVALID_PROPERTY);
			}
			else {
				try {
					Object o = XMLToGraph.deserialize(pvalue, nc.getProperty(pname).getPropertyClass());
					nc.setPropertyValue(pname, o);
				}
				catch (Exception e) {
					return new Message(Message.REPLY_INVALID_VALUE);
				}
			}
			return new Message(Message.REPLY_OK);
		}
		else if (msg.getCommand() == Message.CMD_QUERY_PROPERTIES) {
			String nodeid = (String) msg.getArgs().get(0);
			NodeComponent nc = findNode(root, nodeid);
			if (nc == null) {
				return new Message(Message.REPLY_INVALID_NODEID);
			}
			Collection c = nc.getProperties();
			Iterator i = c.iterator();
			StringBuffer sb = new StringBuffer();
			while (i.hasNext()) {
				Property p = (Property) i.next();
				sb.append(p.getName());
				sb.append(":");
				sb.append(p.getPropertyClass());
				sb.append("(");
				if (p.isWritable()) {
					sb.append("RW)");
				}
				else {
					sb.append("RO)");
				}
				sb.append("=");
				sb.append(p.getValue());
				sb.append("\n");
			}
			Message reply = new Message(Message.REPLY_OK);
			reply.addArg(sb.toString());
			return reply;
		}
		else if (msg.getCommand() == Message.CMD_ADD_PROPERTY) {
			String nodeid = (String) msg.getArgs().get(0);
			NodeComponent nc = findNode(root, nodeid);
			if (nc == null) {
				return new Message(Message.REPLY_INVALID_NODEID);
			}
			String propName = (String) msg.getArgs().get(1);
			if (propName == null) {
				return new Message(Message.REPLY_INVALID_PROPERTY);
			}
			nc.addProperty(new OverlayedProperty(nc, propName));
			return new Message(Message.REPLY_OK);
		}
		else if (msg.getCommand() == Message.CMD_REMOVE_PROPERTY) {
			String nodeid = (String) msg.getArgs().get(0);
			NodeComponent nc = findNode(root, nodeid);
			if (nc == null) {
				return new Message(Message.REPLY_INVALID_NODEID);
			}
			String propName = (String) msg.getArgs().get(1);
			if (propName == null) {
				return new Message(Message.REPLY_INVALID_PROPERTY);
			}
			if (!nc.hasProperty(propName)) {
				return new Message(Message.REPLY_INVALID_PROPERTY);
			}
			nc.removeProperty(nc.getProperty(propName));
			return new Message(Message.REPLY_OK);
		}
		else {
			return new Message(Message.REPLY_INVALID_COMMAND);
		}
	}

	private NodeComponent findNode(NodeComponent root, String nodeid) {
		if (nodeid.equals(root.getPropertyValue("nodeid"))) {
			return root;
		}
		GraphCanvas canvas = root.getCanvas();
		if (canvas == null) {
			return null;
		}
		GraphInterface gi = canvas.getGraph();
		Iterator i = gi.getNodesIterator();
		NodeComponent result = null;
		while (i.hasNext()) {
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			result = findNode(nc, nodeid);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
