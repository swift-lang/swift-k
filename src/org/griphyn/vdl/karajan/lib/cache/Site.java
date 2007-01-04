/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.util.VDL2Profile;

public class Site {
	public static final Logger logger = Logger.getLogger(Site.class);
	
	private long storageSize;
	private TreeMap accessTimes;
	private Map files;
	private long usage;

	public Site(Object host) {
		accessTimes = new TreeMap();
		files = new HashMap();

		long quota = Long.MAX_VALUE;
		if (host instanceof BoundContact) {
			BoundContact bc = (BoundContact) host;
			Object ss = bc.getProperty(VDL2Profile.KEY_STORAGE_SIZE);
			if (ss != null) {
				quota = TypeUtil.toLong(ss);
				if (quota < 0) {
					quota = Long.MAX_VALUE;
				}
			}
		}
		this.storageSize = quota;
		if (logger.isDebugEnabled()) {
			logger.debug("NewCacheSite(host=" + host + ", storageSize=" + quota + ")");
		}
	}

	public long getStorageSize() {
		return storageSize;
	}

	public synchronized CacheReturn addFile(File f) {
		boolean exists = false;
		File cached = (File) files.get(f.getPath());
		if (cached == null) {
			files.put(f.getPath(), f);
			cached = f;
			usage += cached.getSize();
		}
		else {
			exists = true;
			accessTimes.remove(new Long(cached.getLastAccess()));
		}
		accessTimes.put(new Long(System.currentTimeMillis()), cached);
		if (logger.isDebugEnabled()) {
			logger.debug("CacheAddFile(file=" + f + ", usage=" + usage + "/" + storageSize
					+ ", hit=" + exists + ")");
		}
		return new CacheReturn(exists, purge(), cached);
	}

	public synchronized CacheReturn addAndLockFile(File f) {
		CacheReturn cr = addFile(f);
		cr.cached.lock();
		if (logger.isDebugEnabled()) {
			logger.debug("CacheLockFile(file=" + f + ")");
		}
		return cr;
	}

	public synchronized CacheReturn fileRemoved(File f) {
		File cached = (File) files.remove(f.getPath());
		if (cached == null) {
			throw new IllegalStateException(
					"fileRemoved() called with a file that is not in the cache (" + f + ")");
		}
		if (!cached.isLockedForRemoval()) {
			throw new IllegalStateException(
					"fileRemoved() called on a file that was not locked for removal (" + f + ")");
		}
		usage -= cached.getSize();
		if (logger.isDebugEnabled()) {
			logger.debug("CacheFileRemoved(file=" + cached + ", usage=" + usage + "/" + storageSize
					+ ")");
		}
		cached.notifyListeners();
		return new CacheReturn(true, Collections.EMPTY_LIST, cached);
	}

	public synchronized CacheReturn unlockEntry(File f) {
		File cached = (File) files.get(f.getPath());
		if (cached == null) {
			throw new IllegalStateException(
					"unlockEntry() called with a file that is not in the cache (" + f + ")");
		}
		cached.unlock();
		if (logger.isDebugEnabled()) {
			logger.debug("CacheUnlockFile(file=" + cached + ")");
		}
		return new CacheReturn(true, purge(), cached);
	}

	private synchronized List purge() {
		List l = new ArrayList();
		long targetUsage = usage;
		Iterator i = accessTimes.values().iterator();
		while (i.hasNext() && targetUsage > storageSize) {
			File f = (File) i.next();
			if (!f.isLocked()) {
				f.lockForRemoval();
				l.add(f.getPath());
				targetUsage -= f.getSize();
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("CachePurge(count=" + l.size() + ", targetUsage=" + targetUsage + "/"
					+ storageSize + ")");
		}
		return l;
	}
}
