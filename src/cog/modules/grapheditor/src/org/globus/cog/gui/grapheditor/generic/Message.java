
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jul 1, 2003
 */
package org.globus.cog.gui.grapheditor.generic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class Message {
	private static Logger logger  = Logger.getLogger(Message.class);
	
	private static Hashtable commands;
	
	public static final byte CMD_READ_GRAPH = 0x01;
	public static final byte CMD_UPDATE_PROPERTY = 0x02;
	public static final byte CMD_CLOSE = 0x00;
	public static final byte CMD_QUERY_PROPERTIES = 0x03;
	public static final byte CMD_VERSION = 0x04;
	public static final byte CMD_ADD_PROPERTY = 0x05;
	public static final byte CMD_REMOVE_PROPERTY = 0x06;
	public static final byte REPLY_OK = 0x10;
	public static final byte REPLY_INVALID_NODEID = 0x12;
	public static final byte REPLY_INVALID_PROPERTY = 0x13;
	public static final byte REPLY_READ_ONLY_PROPERTY = 0x14;
	public static final byte REPLY_INVALID_VALUE = 0x15;
	public static final byte REPLY_INVALID_COMMAND = 0x11;
	public static final byte REPLY_UNKNOWN_ERROR = 0x1f;
	
	static {
		//so that toString() gives some meaningful info
		commands = new Hashtable();
		commands.put(new Integer(CMD_READ_GRAPH), "CMD_READ_GRAPH");
		commands.put(new Integer(CMD_UPDATE_PROPERTY), "CMD_UPDATE_PROPERTY");
		commands.put(new Integer(CMD_CLOSE), "CMD_CLOSE");
		commands.put(new Integer(CMD_QUERY_PROPERTIES), "CMD_LIST_PROPERTIES");
		commands.put(new Integer(CMD_VERSION), "CMD_VERSION");
		commands.put(new Integer(CMD_ADD_PROPERTY), "CMD_ADD_PROPERTY");
		commands.put(new Integer(CMD_REMOVE_PROPERTY), "CMD_REMOVE_PROPERTY");
		commands.put(new Integer(REPLY_OK), "REPLY_OK");
		commands.put(new Integer(REPLY_INVALID_NODEID), "REPLY_INVALID_NODEID");
		commands.put(new Integer(REPLY_INVALID_PROPERTY), "REPLY_INVALID_PROPERTY");
		commands.put(new Integer(REPLY_READ_ONLY_PROPERTY), "REPLY_READ_ONLY_PROPERTY");
		commands.put(new Integer(REPLY_INVALID_VALUE), "REPLY_INVALID_VALUE");
		commands.put(new Integer(REPLY_INVALID_COMMAND), "REPLY_INVALID_COMMAND");
		commands.put(new Integer(REPLY_UNKNOWN_ERROR), "REPLY_UNKNOWN_ERROR");
	}

	private byte command;
	private int datalen;
	private List args;

	public Message() {
		args = new LinkedList();
	}

	public Message(byte command) {
		this();
		this.command = command;
	}

	public void read(DataInputStream dis) throws IOException {
		command = dis.readByte();
		logger.debug("CMD: "+command);
		datalen = readInt(dis);
		logger.debug("DATALEN: "+datalen);
		byte[] buf = new byte[datalen];
		dis.readFully(buf, 0, datalen);
		logger.debug("ARGS:");
		int p = 0, q = 0;
		while (q < datalen) {
			if (buf[q] == 0) {
				args.add(new String(buf, p, q - p, "US-ASCII"));
				logger.debug(args.get(args.size()-1));
				p = q + 1;
			}
			q++;
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.write(command);
		int len = 0;
		Iterator i = args.iterator();
		while (i.hasNext()) {
			len += ((String) i.next()).length() + 1;
		}
		writeInt(dos, len);
		i = args.iterator();
		while (i.hasNext()) {
			dos.write(((String) i.next()).getBytes("US-ASCII"));
			dos.write((byte) 0x00);
		}
	}

	public List getArgs() {
		return args;
	}

	public byte getCommand() {
		return command;
	}

	public void setArgs(List strings) {
		this.args = strings;
	}

	public void addArg(String arg) {
		args.add(arg);
	}

	public void setCommand(byte b) {
		this.command = b;
	}

	private int readInt(DataInputStream dis) throws IOException {
		int b0 = dis.readByte(), b1 = dis.readByte(), b2 = dis.readByte(), b3 = dis.readByte();
		return ((b0 & 0xff) << 24) + ((b1 & 0xff) << 16) + ((b2 & 0xff) << 8) + (b3 & 0xff);
	}

	private void writeInt(DataOutputStream dos, int v) throws IOException {
		dos.write((byte) ((v >> 24) & 0xff));
		dos.write((byte) ((v >> 16) & 0xff));
		dos.write((byte) ((v >> 8) & 0xff));
		dos.write((byte) (v & 0xff));
	}
	
	public String toString(){
		String r = (String) commands.get(new Integer(command));
		if (r == null) {
			r = command+" ";
		}
		else {
			r = r + " ";
		}
		Iterator i = getArgs().iterator();
		while (i.hasNext()){
			r = r + i.next() + ", ";
		}
		return r;
	}
}
