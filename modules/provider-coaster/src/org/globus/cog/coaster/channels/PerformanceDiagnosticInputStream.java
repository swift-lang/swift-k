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
 * Created on Jan 16, 2011
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

public class PerformanceDiagnosticInputStream extends InputStream {
	public static final Logger logger = Logger.getLogger(PerformanceDiagnosticInputStream.class);

	private InputStream delegate;
	private static volatile long bytes, last, lastTime, firstTime;
	private static int count = 1;
	
	public static final int INTERVAL = 10; //seconds

	static {
		Timer.every(1000, new Runnable() {
			public void run() {
				count += 1;
				if (count % INTERVAL == 0 && getTotal() > 0) {
    				String s;
    				logger.info(s = "[IN] Total transferred: " + units(getTotal()) + "B, current rate: "
    						+ units(getCurrentRate()) + "B/s, average rate: " + units(getAverageRate())
    						+ "B/s");
				}
				last = bytes;
			}
		});
	}

	public PerformanceDiagnosticInputStream(InputStream delegate) {
		this.delegate = delegate;
	}
	
	public static long getTotal() {
		return bytes;
	}
	
	public static long getCurrentRate() {
		return bytes - last;
	}
	
	public static long getAverageRate() {
		return bytes / count;
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
	
	public static void bytesRead(int count) {
		if (count >= 0) {
			bytes += count;
		}
	}

	public int read(byte[] b) throws IOException {
		int read = delegate.read(b);
		if (read >= 0) {
			bytes += read;
		}
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
