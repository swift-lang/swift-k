//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.io.IOException;
import java.io.InputStream;

class BinaryStreamProcessor extends StreamProcessor {
	private InputStream stream;
	private byte[] buffer;
	private int crt;
	private boolean closed;
	private BinaryOutputListener listener;

	public BinaryStreamProcessor(InputStream stream, BinaryOutputListener listener) {
		this.stream = stream;
		this.listener = listener;
		buffer = new byte[1024];
		crt = 0;
	}

	public boolean poll() {
		if (closed) {
			return false;
		}
		try {
			int avail = stream.available();
			if (avail > 0) {
				int count = Math.min(buffer.length - crt, avail);
				crt += stream.read(buffer, crt, count);
				if (crt == buffer.length) {
					commit();
				}
			}
			return true;
		}
		catch (IOException e) {
			close();
			return false;
		}
	}
	
	public void close() {
		poll();
		commit();
		closed = true;
	}
	
	protected void commit() {
		if (crt > 0) {
			listener.dataReceived(buffer, 0, crt);
			crt = 0;
		}
	}

	protected boolean isClosed() {
		return closed;
	}
}