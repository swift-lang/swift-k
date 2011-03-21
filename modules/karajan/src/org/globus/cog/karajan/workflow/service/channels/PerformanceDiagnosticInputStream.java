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
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

public class PerformanceDiagnosticInputStream extends InputStream {
	public static final Logger logger = Logger.getLogger(PerformanceDiagnosticInputStream.class);

	private InputStream delegate;
	private static volatile long bytes, last;
	private static int count;

	static {
		Timer.every(1000, new Runnable() {
			public void run() {
				count++;
				String s;
				logger.info(s = "Total transferred: " + units(bytes) + "B, current rate: "
						+ units(bytes - last) + "B/s, average rate: " + units(bytes / count)
						+ "B/s");
				System.out.println("[IN]: " + s);
				System.out.println("[MEM] Heap total: "
						+ units(Runtime.getRuntime().totalMemory())
						+ "MB, Heap used: "
						+ units(Runtime.getRuntime().totalMemory()
								- Runtime.getRuntime().freeMemory()) + "MB");
				last = bytes;
			}
		});
	}

	public PerformanceDiagnosticInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	private static final String[] U = { "", "K", "M", "G" };
	private static final NumberFormat NF = new DecimalFormat("###.##");

	public static String units(long v) {
		double dv = v;
		int index = 0;
		while (dv > 1024 && index < U.length - 1) {
			dv = dv / 1024;
			index++;
		}
		return NF.format(dv) + " " + U[index];
	}

	public int read() throws IOException {
		bytes++;
		return delegate.read();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public int read(byte[] b) throws IOException {
		int read = delegate.read(b);
		bytes += read;
		return read;
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int read = delegate.read(b, off, len);
		bytes += read;
		return read;
	}

	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	public String toString() {
		return delegate.toString();
	}

	public int available() throws IOException {
		return delegate.available();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	public void reset() throws IOException {
		delegate.reset();
	}

	public boolean markSupported() {
		return delegate.markSupported();
	}
}
