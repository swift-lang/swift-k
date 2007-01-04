/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.LinkedList;

public class File {
	private String path;
	private Object host;
	private long size, lastAccess;
	private int locked;
	private boolean removalLock;
	private LinkedList removalListeners;

	public File(String file, String dir, Object host, long size) {
		if (dir.endsWith("/")) {
			path = dir + file;
		}
		else {
			path = dir + '/' + file;
		}
		this.host = host;
		this.size = size;
	}

	public File(String fullPath, Object host, long size) {
		this.path = fullPath;
		this.host = host;
		this.size = size;
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
		return host + "/" + path;
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
	public void lockForRemoval() {
		removalLock = true;
	}

	public boolean isLockedForRemoval() {
		return removalLock;
	}

	public synchronized void addRemovalListener(RemovalListener l, Object param) {
		if (removalListeners == null) {
			removalListeners = new LinkedList();
		}
		removalListeners.add(new Object[] { l, param });
	}

	public synchronized void notifyListeners() {
		if (removalListeners != null) {
			while (removalListeners.size() > 0) {
				Object[] p = (Object[]) removalListeners.removeFirst();
				((RemovalListener) p[0]).fileRemoved(this, p[1]);
			}
		}
	}
}