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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.service.channels.AbstractKarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public abstract class RequestReply {
	public static final Logger logger = Logger.getLogger(RequestReply.class);
	
	public static final int DEFAULT_TIMEOUT = 120 * 1000;
	private int timeout = DEFAULT_TIMEOUT;

	public static final int NOID = -1;
	private int id;
	private String outCmd;
	private String inCmd;
	private List<byte[]> outData;
	private List<byte[]> inData;
	private boolean inDataReceived;
	private KarajanChannel channel;
	private long lastTime = Long.MAX_VALUE;

	// private static final byte[] NO_EXCEPTION = new byte[0];

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
	 
	/**
	 * @deprecated Use setChannel
	 */
	public void register(KarajanChannel channel) {
		this.channel = channel;
	}

	protected synchronized void addOutData(byte[] data) {
		if (this.outData == null) {
			this.outData = new LinkedList<byte[]>();
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

	protected static byte[] pack(long value) {
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
	    if (logger.isInfoEnabled()) {
	    	logger.info(this + " sending error: " + error, e);
	    }
		if (error == null) {
			if (e == null) {
				error = "No message available";
			}
			else {
				error = (e.getMessage() == null ? e.toString() : e.getMessage());
			}
		}
		this.addOutData(error.getBytes());
		try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(baos);
    		if (e != null) {
    		    oos.writeObject(e);
    		}
    		oos.close();
    		this.addOutData(baos.toByteArray());
		}
		catch (IOException ex) {
		    logger.warn("Exception caught serializing exception", e);
		}
		send(true);
	}
	
	public void send() throws ProtocolException {
		send(false);
	}
	
	public abstract void send(boolean err) throws ProtocolException;
	
	protected void dataReceived(boolean fin, boolean error, byte[] data) throws ProtocolException {
		setLastTime(System.currentTimeMillis());
	}
		
	protected synchronized void addInData(boolean fin, boolean err, byte[] data) {
		if (inData == null) {
			inData = new ArrayList<byte[]>(4);
		}
		inData.add(data);
	}
	
	protected final void addInData(byte[] data) {
		throw new RuntimeException("Should not be used");
	}

	public void receiveCompleted() {
		synchronized (this) {
			inDataReceived = true;
			notify();
		}
	}

	public List<byte[]> getInDataChunks() {
		return inData;
	}

	public byte[] getInData() {
		if (inData == null) {
			return null;
		}
		int len = 0;
		for (byte[] chunk : inData) {
			len += chunk.length;
		}

		byte[] data = new byte[len];
		len = 0;
		for (byte[] chunk : inData) {
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
			return inData.get(index);
		}
		catch (IndexOutOfBoundsException e) {
		    List<String> l = new ArrayList<String>();
		    for (int i = 0; i < inData.size(); i++) {
		        l.add(new String(inData.get(i)));
		    }
			throw new IllegalArgumentException("Missing command argument #" + (index + 1) + "; inData: " + l);
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

	public static long unpackLong(byte[] b) {
		if (b.length != 8) {
			throw new IllegalArgumentException("Wrong data size: " + b.length + ". Data was "
					+ AbstractKarajanChannel.ppByteBuf(b));
		}
		long l = 0;
		for (int i = 7; i >=0 ; i--) {
		    l <<= 8;
		    l += b[i] & 0xff;
		}
		return l;
	}
	
	public static void main(String[] args) {
	    System.out.println(unpackLong(pack(1L)));
	    System.out.println(unpackLong(pack(10L)));
	    System.out.println(unpackLong(pack(1000000000L)));
	    System.out.println(unpackLong(pack(10000000000000L)));
	}

	public boolean getInDataAsBoolean(int index) {
		return getInDataAsInt(index) != 0;
	}

	public synchronized void setInData(int index, byte[] data) {
		if (inData == null) {
			inData = new LinkedList<byte[]>();
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

	public void setChannel(KarajanChannel channel) {
		this.channel = channel;
	}

	public Collection<byte[]> getOutData() {
		return outData;
	}

	public abstract void errorReceived(String msg, Exception t);

	public void errorReceived() {
		String msg = null;
		Exception exception = null;
		List<byte[]> data = getInDataChunks();
		if (data != null && data.size() > 0) {
			msg = new String(data.get(0));
			if (data.size() > 1) {
			    try {
    			    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.get(1)));
    				exception = new RemoteException(msg, (Exception) ois.readObject());
    				ois.close();
			    }
			    catch (Exception e) {
			        logger.warn("Failed to de-serialize remote exception", e);
			    }
			}
		}
		errorReceived(msg, exception);
	}

	protected String ppData(String prefix, String cmd, 
			                Collection<byte[]> data) {
		StringBuffer sb = new StringBuffer();
		sb.append(getChannel());
		sb.append(": ");
		sb.append(prefix);
		sb.append(getId());
		sb.append(' ');
		sb.append(cmd);
		if (data != null && data.size() != 0) {
			sb.append('(');
			Iterator<byte[]> i = data.iterator();
			while (i.hasNext()) {
				byte[] buf = i.next();
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

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void handleTimeout() {
		logger.warn("Unhandled timeout", new Throwable());
		setLastTime(Long.MAX_VALUE);
	}
	
	public void handleSignal(byte[] data) {
		
	}
}
