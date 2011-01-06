//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 16, 2005
 */
package org.globus.cog.karajan.util;

import java.util.Map;


public class MapKeyKarajanIterator extends AbstractKarajanIterator {
	private final Map map;
	
	public MapKeyKarajanIterator(Map map) {
		//The big question here is whether a de-serialized map will iterate
		//the same as the one before serialization. As in the same order for
		//the keys. I'm pretty sure that can't be guaranteed. Which means that
		//this may break checkpointing!
		//TODO in other words FIX ME!
		super(map.keySet().iterator());
		this.map = map;
	}

	public void reset() {
		setIterator(map.keySet().iterator());
	}

	public int count() {
		return map.size();
	}
}
