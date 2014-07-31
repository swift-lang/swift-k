/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import k.rt.AbstractFuture;
import k.rt.Future;

import org.globus.cog.karajan.futures.FutureEvaluationException;

public class File extends AbstractFuture implements Future {
	private String path;
	private Object host;
	private long size, lastAccess;
	private int locked;
	private boolean processingLock;

	public File(String file, String dir, Object host, long size) {
		if (dir.endsWith("/")) {
			path = dir + file;
		}
		else {
			path = dir + '/' + file;
		}
		path = normalize(path);
		this.host = host;
		this.size = size;
	}

	public File(String fullPath, Object host, long size) {
		this.path = normalize(fullPath);
		this.host = host;
		this.size = size;
	}
	
	private String normalize(String path) {
		if (path.indexOf("//") == -1) {
			return path;
		}
		else {
			StringBuffer sb = new StringBuffer();
			boolean lastWasSlash = false;
			for(int i = 0; i < path.length(); i++) {
				char c = path.charAt(i);
				if (c == '/') {
					if (!lastWasSlash) {
						sb.append(c);
					}
					lastWasSlash = true;
				}
				else {
					sb.append(c);
					lastWasSlash = false;
				}
			}
			return sb.toString();
		}
	}

	public boolean equals(Object other) {
		if (other instanceof File) {
			File ce = (File) other;
			return path.equals(ce.path) && host.equals(ce.host);
		}
		return false;
	}

	public int hashCode() {
		return path.hashCode() + host.hashCode();
	}

	public String toString() {
		return host + ":" + path;
	}

	public String getPath() {
		return path;
	}

	public Object getHost() {
		return host;
	}

	public void setHost(Object host) {
		this.host = host;
	}

	public synchronized boolean isLocked() {
		return locked > 0;
	}

	public synchronized void lock() {
		locked++;
	}

	public synchronized boolean unlock() {
		locked--;
		return locked == 0;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public long touch() {
		try {
			return lastAccess;
		}
		finally {
			lastAccess = System.currentTimeMillis();
		}
	}

	/**
	 * This means that the cache has decided that the file should be removed and
	 * nothing else can be done on it. It cannot be added or removed from the
	 * cache.
	 */
	public synchronized void lockForProcessing() {
		processingLock = true;
	}
	
	public synchronized void unlockFromProcessing() {
		processingLock = false;
		notifyListeners();
	}

	public boolean isLockedForProcessing() {
		return processingLock;
	}

	public void close() {
	}

	public void fail(FutureEvaluationException e) {
	}

	public Object getValue() {
		return null;
	}

	public synchronized boolean isClosed() {
		return !processingLock;
	}
}