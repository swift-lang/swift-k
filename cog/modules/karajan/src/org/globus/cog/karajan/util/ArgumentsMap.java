//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 2, 2005
 */
package org.globus.cog.karajan.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.arguments.Arg;

public final class ArgumentsMap {

	private final Map validArgs;
	private final Set vargs;
	private final Map sortedArgs;
	private final Map maxIndices;
	private final Map optional;
    private final Map channels;
	
	private ArgumentsMap() {
		validArgs = new HashMap();
		sortedArgs = new HashMap();
		maxIndices = new HashMap();
		vargs = new HashSet();
		optional = new HashMap();
        channels = new HashMap();
	}
	
	private static ArgumentsMap map;
	
	public synchronized static ArgumentsMap getMap() {
		if (map == null) {
			map = new ArgumentsMap();
		}
		return map;
	}

	public Set getVargs() {
		return vargs;
	}

	public Map getValidArgs() {
		return validArgs;
	}

	public Map getSortedArgs() {
		return sortedArgs;
	}
	
	public Map getOptionals() {
		return optional;
	}

	public Map getMaxIndices() {
		return maxIndices;
	}

	public void addChannel(Object owner, Arg.Channel channel) {
        List l = (List) channels.get(owner);
        if (l == null) {
        	l = new ArrayList();
            channels.put(owner, l);
        }
        l.add(channel);
	}
    
    public List getChannels(Object owner) {
    	return (List) channels.get(owner);
    }
}
