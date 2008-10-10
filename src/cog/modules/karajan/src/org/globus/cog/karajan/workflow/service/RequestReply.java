//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.service.channels.AbstractKarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public abstract class RequestReply {

	public static final int NOID = -1;
	private int id;
	private String outCmd;
	private String inCmd;
	private List outData;
	private List inData;
	private boolean inDataReceived, errorFlag;
	private KarajanChannel channel;

	private static final byte[] NO_EXCEPTION = new byte[0];

	protected String getInCmd() {
		return inCmd;
	}

	protected void setInCmd(String inCmd) {
		this.inCmd = inCmd;
	}

	protected String getOutCmd() {
		return outCmd;
	}

	protected void setOutCmd(String outCmd) {
		this.outCmd = outCmd;
	}

	public void register(KarajanChannel channel) {
		this.channel = channel;
	}

	protected synchronized void addOutData(byte[] data) {
		if (this.outData == null) {
			this.outData = new LinkedList();
		}
		this.outData.add(data);
	}

	protected void addOutData(String str) {
		this.addOutData(str.getBytes());
	}

	protected void addOutData(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >> 8) & 0xff);
		b[2] = (byte) ((value >> 16) & 0xff);
		b[3] = (byte) ((value >> 24) & 0xff);
		addOutData(b);
	}

	protected void addOutData(long value) {
		addOutData(pack(value));
	}

	protected byte[] pack(long value) {
		byte[] b = new byte[8];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >> 8) & 0xff);
		b[2] = (byte) ((value >> 16) & 0xff);
		b[3] = (byte) ((value >> 24) & 0xff);
		b[4] = (byte) ((value >> 32) & 0xff);
		b[5] = (byte) ((value >> 40) & 0xff);
		b[6] = (byte) ((value >> 48) & 0xff);
		b[7] = (byte) ((value >> 56) & 0xff);
		return b;
	}

	protected void addOutData(boolean value) {
		addOutData(value ? 1 : 0);
	}

	protected void sendError(String error) throws ProtocolException {
		sendError(error, null);
	}

	public void sendError(String error, Throwable e) throws ProtocolException {
		raiseErrorFlag();
		this.addOutData(error.getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (e != null) {
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
		}
		this.addOutData(baos.toByteArray());
		send();
	}

	public void raiseErrorFlag() {
		errorFlag = true;
	}

	protected boolean getErrorFlag() {
		return errorFlag;
	}

	public abstract void send() throws ProtocolException;

	protected void dataReceived(byte[] data) throws ProtocolException {
	}

	protected synchronized void addInData(byte[] data) {
		if (inData == null) {
			inData = new ArrayList(4);
		}
		inData.add(data);
	}

	public void receiveCompleted() {
		synchronized (this) {
			inDataReceived = true;
			notify();
		}
	}

	public List getInDataChuncks() {
		return inData;
	}

	public byte[] getInData() {
		if (inData == null) {
			return null;
		}
		int len = 0;
		Iterator i = inData.iterator();
		while (i.hasNext()) {
			byte[] chunk = (byte[]) i.next();
			len += chunk.length;
		}

		byte[] data = new byte[len];
		len = 0;
		i = inData.iterator();
		while (i.hasNext()) {
			byte[] chunk = (byte[]) i.next();
			System.arraycopy(chunk, 0, data, len, chunk.length);
			len += chunk.length;
		}
		return data;
	}

	public synchronized byte[] getInData(int index) {
		if (inData == null) {
			return null;
		}
		try {
			return (byte[]) inData.get(index);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Missing command argument #" + (index + 1));
		}
	}

	public synchronized int getInDataSize() {
		return inData.size();
	}

	public String getInDataAsString(int index) {
		return new String(getInData(index));
	}

	public int getInDataAsInt(int index) {
		byte[] b = getInData(index);
		if (b.length != 4) {
			throw new IllegalArgumentException("Wrong data size: " + b.length + ". Data was "
					+ AbstractKarajanChannel.ppByteBuf(b));
		}
		return b[0] + (b[1] << 8) + (b[2] << 16) + (b[3] << 24);
	}

	public long getInDataAsLong(int index) {
		return unpackLong(getInData(index));

	}

	protected long unpackLong(byte[] b) {
		if (b.length != 8) {
			throw new IllegalArgumentException("Wrong data size: " + b.length + ". Data was "
					+ AbstractKarajanChannel.ppByteBuf(b));
		}
		return b[0] + (b[1] << 8) + (b[2] << 16) + (b[3] << 24) + (b[2] << 32) + (b[3] << 40)
				+ (b[2] << 48) + (b[3] << 56);
	}

	public boolean getInDataAsBoolean(int index) {
		return getInDataAsInt(index) != 0;
	}

	public synchronized void setInData(int index, byte[] data) {
		if (inData == null) {
			inData = new LinkedList();
		}
		inData.set(index, data);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isInDataReceived() {
		return inDataReceived;
	}

	public KarajanChannel getChannel() {
		return channel;
	}

	protected void setChannel(KarajanChannel channel) {
		this.channel = channel;
	}

	public Collection getOutData() {
		return outData;
	}

	public abstract void errorReceived(String msg, Exception t);

	public void errorReceived() {
		String msg = null;
		Exception exception = null;
		List data = getInDataChuncks();
		if (data.size() > 0) {
			msg = new String((byte[]) data.get(0));
			if (data.size() > 1) {
				String ex = new String((byte[]) data.get(1));
				exception = new RemoteException(msg, ex);
			}
		}
		errorReceived(msg, exception);
	}

	protected String ppData(String prefix, String cmd, Collection data) {
		StringBuffer sb = new StringBuffer();
		sb.append(getChannel());
		sb.append(": ");
		sb.append(prefix);
		sb.append(getId());
		sb.append(' ');
		sb.append(cmd);
		if (data != null && data.size() != 0) {
			sb.append('(');
			Iterator i = data.iterator();
			while (i.hasNext()) {
				byte[] buf = (byte[]) i.next();
				sb.append(AbstractKarajanChannel.ppByteBuf(buf));
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append(')');
		}
		return sb.toString();
	}

	public void channelClosed() {
	}

	public static byte[] serialize(Object obj) throws ProtocolException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.BEST_SPEED);
		OutputStreamWriter osw = new OutputStreamWriter(new DeflaterOutputStream(baos, deflater));
		try {
			XMLConverter.serializeObject(obj, osw);
			osw.close();
			baos.close();
		}
		catch (IOException e) {
			throw new ProtocolException("Could not serialize instance", e);
		}
		return baos.toByteArray();
	}

	public static Object deserialize(byte[] data) {
		Inflater inflater = new Inflater();
		InputStreamReader isr = new InputStreamReader(new InflaterInputStream(
				new ByteArrayInputStream(data), inflater));
		// TODO on a shared service deserialization should always be restricted.
		// Always!!
		return XMLConverter.readObject(isr);
	}

	protected void addOutObject(Object obj) throws ProtocolException {
		addOutData(serialize(obj));
	}

	protected Object getInObject(int index) {
		return deserialize(getInData(index));
	}
}
