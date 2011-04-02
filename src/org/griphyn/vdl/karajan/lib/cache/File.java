/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;

public class File implements Future {
	private String path;
	private Object host;
	private long size, lastAccess;
	private int locked;
	private boolean processingLock;
	private List listeners;

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

	public void notifyListeners() {
		if (listeners != null) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				EventTargetPair etp = (EventTargetPair) i.next();
				i.remove();
				EventBus.post(etp.getTarget(), etp.getEvent());
			}
		}
	}

	public synchronized void addModificationAction(EventListener target, Event event) {
		if (listeners == null) {
			listeners = new LinkedList();
		}
		listeners.add(new EventTargetPair(event, target));
		if (isClosed()) {
			notifyListeners();
		}
	}

	public void close() {
	}

	public void fail(FutureEvaluationException e) {
	}

	public Object getValue() throws ExecutionException {
		return null;
	}

	public synchronized boolean isClosed() {
		return !processingLock;
	}
}