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

class SudoErrorStreamProcessor extends StreamProcessor {
	public static final String WRAPPER_OK = "WRAPPER OK";

	private InputStream stream;
	private byte[] buffer;
	private int crt;
	private boolean closed, transparent, misconfigured;
	private BinaryOutputListener listener;
	private StringBuffer fullOutput;
	private ProcessListener plistener;

	public SudoErrorStreamProcessor(InputStream stream, BinaryOutputListener listener,
			ProcessListener plistener) {
		this.stream = stream;
		this.listener = listener;
		this.plistener = plistener;
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
		closed = true;
		commit();
	}

	protected void commit() {
		try {
			if (crt > 0) {
				if (transparent) {
					listener.dataReceived(buffer, 0, crt);
				}
				else {
					if (misconfigured) {
						fullOutput.append(new String(buffer, 0, crt));
					}
					else {
						String buf = new String(buffer, 0, crt);
						if (buf.startsWith(WRAPPER_OK)) {
							if (crt > WRAPPER_OK.length()) {
								listener.dataReceived(buffer, WRAPPER_OK.length(), crt
										- WRAPPER_OK.length());
							}
							transparent = true;
						}
						else {
							fullOutput = new StringBuffer();
							fullOutput.append(buf);
							misconfigured = true;
						}
					}
					if (closed && misconfigured) {
						plistener.processFailed(getError());
					}
				}
			}
			else {
				if (closed && !transparent) {
					plistener.processFailed(getError());
				}
			}
		}
		finally {
			crt = 0;
		}
	}

	protected boolean isClosed() {
		return closed;
	}

	public boolean isMisconfigured() {
		return misconfigured;
	}

	public String getError() {
		if (fullOutput == null) {
			return "Wrapper does not work properly";
		}
		String out = fullOutput.toString();
		if (out.indexOf("Password:") != -1) {
			return "Misconfigured SUDO";
		}
		else {
			return fullOutput.toString();
		}
	}
}