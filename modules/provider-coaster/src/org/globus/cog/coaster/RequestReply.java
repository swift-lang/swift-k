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
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.channels.AbstractCoasterChannel;
import org.globus.cog.coaster.channels.CoasterChannel;

public abstract class RequestReply {
	public static final Logger logger = Logger.getLogger(RequestReply.class);
		
	public static final int NOID = -1;
	private int id;
	private String outCmd;
	private String inCmd;
	private List<byte[]> outData;
	private List<byte[]> inData;
	private boolean inDataReceived;
	private CoasterChannel channel;
	
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
	public void register(CoasterChannel channel) {
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

	public synchronized byte[] getInData() {
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
					+ AbstractCoasterChannel.ppByteBuf(b));
		}
		return b[0] + (b[1] << 8) + (b[2] << 16) + (b[3] << 24);
	}

	public long getInDataAsLong(int index) {
		return unpackLong(getInData(index));
	}

	public static long unpackLong(byte[] b) {
		if (b.length != 8) {
			throw new IllegalArgumentException("Wrong data size: " + b.length + ". Data was "
					+ AbstractCoasterChannel.ppByteBuf(b));
		}
		long l = 0;
		for (int i = 7; i >=0 ; i--) {
		    l <<= 8;
		    l += b[i] & 0xff;
		}
		return l;
	}
	
	public static void main(String[] args) {
		test(1L);
		test(10L);
		test(1000000000L);
		test(10000000000000L);
		testPerl(1L);
        testPerl(10L);
        testPerl(1000000000L);
        testPerl(10000000000000L);
	}
	
	private static void test(long l) {
		System.out.print("Test " + l);
		if (unpackLong(pack(l)) != l) {
			throw new RuntimeException("Failed: " + l);
		}
		System.out.println(" OK");
	}
	
	private static void testPerl(long l) {
		System.out.print("Test perl " + l);
        if (unpackLong(packPerl(l)) != l) {
            throw new RuntimeException("Failed perl: " + l);
        }
        System.out.println(" OK");
    }
	
	private static byte[] packPerl(long l) {
		try {
			Process p = Runtime.getRuntime().exec(new String[] 
			     {"perl", "-e", "my $ts = " + l + "; print pack(\"VV\", ($ts & 0xffffffff), ($ts >> 32));"});
			int ec = p.waitFor();
			if (ec != 0) {
				return new byte[8];
			}
			byte b[] = new byte[8];
			p.getInputStream().read(b);
			return b;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new byte[8];
		}
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

	public CoasterChannel getChannel() {
		return channel;
	}

	public void setChannel(CoasterChannel channel) {
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
				sb.append(AbstractCoasterChannel.ppByteBuf(buf));
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

	protected void addOutObject(Object obj) throws ProtocolException {
		addOutData(serialize(obj));
	}

	private byte[] serialize(Object obj) {
        throw new UnsupportedOperationException();
    }

    protected Object getInObject(int index) {
		return deserialize(getInData(index));
	}
	
	private Object deserialize(byte[] buf) {
        throw new UnsupportedOperationException();
    }

    public void handleSignal(byte[] data) {
		
	}
}
