//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 30, 2005
 */
package org.globus.cog.karajan.scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WeightedHostSet {
	private TreeMap map;
	private HashMap scores;

	public WeightedHostSet() {
		map = new TreeMap();
		scores = new HashMap();
	}
	
	public synchronized void add(WeightedHost wh) {
		Double score = wh.getScoreAsDouble();
		Set s = (Set) map.get(score);
		if (s == null) {
			s = new HashSet();
			map.put(score, s);
		}
		
		s.add(wh);
		scores.put(wh, score);
	}
	
	public synchronized double remove(WeightedHost wh) {
		Double score = (Double) scores.get(wh);
		Set s = (Set) map.get(score);
		if (s != null) {
			s.remove(wh);
			if (s.isEmpty()) {
				map.remove(score);
			}
			scores.remove(wh);
		}
		return score.doubleValue();
	}
	
	public java.util.Iterator iterator() {
		return new Iterator();
	}
	
	public WeightedHost last() {
		Set s = (Set) map.get(map.lastKey());
		return (WeightedHost) s.iterator().next();
	}
	
	public int size() {
		return scores.size();
	}
	
	public String toString() {
		return map.toString();
	}
	
	private class Iterator implements java.util.Iterator{
		private final java.util.Iterator mapi;
		private java.util.Iterator seti;
		private WeightedHost lasth;
		private Set lasts;
		
		
		public Iterator() {
			mapi = map.entrySet().iterator();
			if (mapi.hasNext()) {
				Entry next = (Entry) mapi.next();
				lasts = (Set) next.getValue();
				seti = lasts.iterator();
			}
		}

		public boolean hasNext() {
			return seti.hasNext() || mapi.hasNext();
		}

		public Object next() {
			if (seti.hasNext()) {
				lasth =  (WeightedHost) seti.next();
				return lasth;
			}
			else if (mapi.hasNext()) {
				Entry next = (Entry) mapi.next();
				lasts = (Set) next.getValue();
				seti = lasts.iterator();
				lasth = (WeightedHost) seti.next();
				return lasth;
			}
			else {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			seti.remove();
			if (lasts.isEmpty()) {
				mapi.remove();
			}
			scores.remove(lasth);
		}
	}

}
