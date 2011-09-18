/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LRUFileCache implements VDLFileCache {
	private Map sites;

	public LRUFileCache() {
		sites = new HashMap();
	}

	public CacheReturn addEntry(File entry) {
		Site s = getSite(entry.getHost());
		return s.addFile(entry);
	}

	public CacheReturn addAndLockEntry(File entry) {
		Site s = getSite(entry.getHost());
		return s.addAndLockFile(entry);
	}

	public CacheReturn entryRemoved(File f) {
		Site s = getSite(f.getHost());
		return s.fileRemoved(f);
	}

	public CacheReturn unlockEntry(File f, boolean force) {
		Site s = getSite(f.getHost());
		return s.unlockEntry(f, force);
	}

	public CacheReturn unlockFromProcessing(File f) {
		Site s = getSite(f.getHost());
		return s.unlockFromProcessing(f);
	}

	protected Site getSite(Object host) {
		synchronized (sites) {
			Site site = (Site) sites.get(host);
			if (site == null) {
				site = new Site(host);
				sites.put(host, site);
			}
			return site;
		}
	}

	public Collection getFiles(Object host) {
		synchronized (sites) {
			Site site = (Site) sites.get(host);
			if (site == null) {
				return Collections.EMPTY_LIST;
			}
			else {
				return site.getFiles();
			}
		}
	}
	
	public Collection getPaths(Object host) {
		synchronized(sites) {
			Site site = (Site) sites.get(host);
			if (site == null) {
				return Collections.EMPTY_LIST;
			}
			else {
				return site.getPaths();
			}
		}
	}
	
	public String toString() {
		return "LRU File Cache";
	}
}
