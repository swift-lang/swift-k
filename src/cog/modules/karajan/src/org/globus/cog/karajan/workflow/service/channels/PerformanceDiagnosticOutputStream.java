//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 16, 2011
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class PerformanceDiagnosticOutputStream extends OutputStream {
	public static final Logger logger = Logger.getLogger(PerformanceDiagnosticOutputStream.class);

	private final OutputStream delegate;

	private static volatile long bytes, last;
	private static int count = 1;
	
	public static final int INTERVAL = 10; //seconds

	static {
		Timer.every(1000 * INTERVAL, new Runnable() {
			public void run() {
				count += INTERVAL;
				String s;
				logger.info(s = "[OUT] Total transferred: "
						+ getTotal() + "B, current rate: "
						+ getCurrentRate()
						+ "B/s, average rate: "
						+ getAverageRate()
						+ "B/s");
				last = bytes;
			}
		});
	}

	public PerformanceDiagnosticOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}
	
	public static String getTotal() {
		return PerformanceDiagnosticInputStream.units(bytes);
	}
	
	public static String getCurrentRate() {
		return PerformanceDiagnosticInputStream.units(bytes - last);
	}
	
	public static String getAverageRate() {
		return PerformanceDiagnosticInputStream.units(bytes / count);
	}
	
	public static void bytesWritten(int count) {
	    bytes += count;
	}

	public void write(int b) throws IOException {
		delegate.write(b);
		bytes++;
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public void write(byte[] b) throws IOException {
		delegate.write(b);
		bytes += b.length;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
		bytes += len;
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void flush() throws IOException {
		delegate.flush();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public String toString() {
		return delegate.toString();
	}
}
