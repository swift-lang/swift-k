/*
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;


public interface VDLFileCache {
	CacheReturn addEntry(File entry);

	CacheReturn addAndLockEntry(File file);

	CacheReturn entryRemoved(File f);

	CacheReturn unlockEntry(File f);
}
