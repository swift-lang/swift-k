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
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.Collection;

/**
 * Describes a client caching mechanism. Classes implementing this interface
 * would not deal with the actual physical data processing required to move data
 * in and out the cache. Instead they would implement the accounting mechanism
 * needed for caching.
 * 
 * The logical flow of the process is as follows:
 * <ol>
 * 	<li> The client code wants to cache an entry
 * 	<li> The client code calls <code>addAndLockEntry</code> with the respective
 * 		entry. This will lock the entry for both usage and processing. The
 * 		<code>addAndLockEntry</code> method returns a <code>CacheReturn</code>
 * 		object (let's call it <code>status</code>), which can signal a few things:
 * 
 * 		<ol>
 * 			<li> If the entry is already cached, then <code>status.alreadyCached</code>
 * 				will be <code>true</code>, and <code>status.cached</code> will contain
 * 				the cached entry. The client code should check then if the already cached
 * 				entry is locked for processing by calling the
 * 				<code>isLockedForProcessing</code> method, and:
 * 				<ol>
 * 					<li> If the entry is locked for processing, the client code should add a
 * 						listener to the entry in order to get notified when the processing is
 * 						complete (<code>status.cached.addProcessingListener</code>). Upon receipt
 * 						of notification of processing completion, the client code should retry
 * 						calling the addAndLockEntry, since the processing may have involved the
 * 						removal of the file from the cache.
 * 					<li> If the entry is not locked for processing, the client code can assume
 * 						that the entry is correctly present in the physical cache
 * 				</ol>
 * 			<li> If the entry is not cached (<code>status.alreadyCached</code> is <code>false</code>), 
 * 				then the client code must physically put the entry into the cache, after which it 
 * 				shoult call <code>unlockFromProcessing</code>
 * 			<li> Removal requests are listed in the <code>status.remove</code> list. The entries
 * 				returned must be already locked for processing by the cache implementation, in 
 * 				order to prevent them from being used by other threads. 
 * 				The application code should physically remove such entries from the cache, and 
 * 				then call <code>fileRemoved</code>.
 * 		</ol> 
 * 	<li> The client code uses the cached data
 * 	<li> The client code calls <code>unlockEntry</code> to signal that the data is not needed by the
 * 		application any more, and it can eventually be removed from the cache
 * </ol>
 * 
 * Alternatively, the <code>addEntry</code> method can be used to add an entry to the cache without
 * locking it in any way. This is intended for cases when the client code does not have direct control
 * over what goes into the physical cache.
 */
public interface VDLFileCache {
	/**
	 * Adds an entry to the cache, but does not lock it.
	 * 
	 * @param entry
	 *            the entry to be added
	 */
	CacheReturn addEntry(File entry);

	/**
	 * Adds an entry to the cache and locks it both for usage and processing. It
	 * is expected that the entry will be unlocked from processing when the
	 * operation that actually does the physical insertion of the entry into the
	 * cache completes successfully. It is also expected that the entry will be
	 * unlocked when it is not used any more, so that it can be purged if
	 * caching policy dictates it.
	 * 
	 * @param file
	 *            the entry to be added
	 * 
	 */
	CacheReturn addAndLockEntry(File file);

	/**
	 * Removes an entry from the cache. This must be called on entries locked
	 * for processing by one of the calls to the other caching functions.
	 */
	CacheReturn entryRemoved(File f);

	/**
	 * Unlocks an entry in the cache.
	 * 
	 * @param f
	 *            the entry to be removed
	 * @param force
	 *            If <code>true</code> then an exception will be thrown if the
	 *            entry is not present in the cache. If <code>false</code>
	 *            then <code>unlockEntry</code> will attempt to unlock an
	 *            entry if it is found in the cache, but will return silently if
	 *            the entry is not in the cache
	 */
	CacheReturn unlockEntry(File f, boolean force);

	/**
	 * Marks an entry as not being processed any more and ready for use (unless
	 * the entry was locked for being removed).
	 */
	CacheReturn unlockFromProcessing(File f);
	
	/**
	 * Returns a collection of {@link File} objects representing the files
	 * stored in the cache for the specified host
	 */
	Collection<File> getFiles(Object host);
	
	/**
	 * Returns a collection of {@link String} objects representing the file paths
	 * stored in the cache for the specified host
	 */
	Collection<String> getPaths(Object host);
}
