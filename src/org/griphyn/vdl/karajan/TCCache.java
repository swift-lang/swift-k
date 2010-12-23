/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.globus.swift.catalog.TransformationCatalog;
import org.globus.swift.catalog.types.TCType;
import org.griphyn.vdl.util.FQN;

public class TCCache {
	private TransformationCatalog tc;
	private Map<Entry, List> cache;
	private Entry entry;

	public TCCache(TransformationCatalog tc) {
		this.tc = tc;
		cache = new HashMap<Entry, List>();
		entry = new Entry();
	}

	public synchronized List getTCEntries(FQN tr, String host, TCType tctype) throws Exception {
		entry.set(tr, host, tctype);
		List l = cache.get(entry);
		if (l == null && !cache.containsKey(entry)) {
			l = tc.getTCEntries(tr.getNamespace(), tr.getName(), tr.getVersion(), host, tctype);
			cache.put(new Entry(tr, host, tctype), l);
		}
		return l;
	}

	private class Entry {
		public FQN tr;
		public String host;
		public TCType tctype;

		public Entry() {
		}

		public Entry(FQN tr, String host, TCType tctype) {
			set(tr, host, tctype);
		}

		public void set(FQN tr, String host, TCType tctype) {
			this.tr = tr;
			this.host = host;
			this.tctype = tctype;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry other = (Entry) obj;
				return tr.equals(other.tr) && host.equals(other.host) && tctype.equals(other.tctype);
			}
			else {
				return false;
			}
		}

		public int hashCode() {
			return tr.hashCode() + host.hashCode() + tctype.hashCode();
		}
	}
}
