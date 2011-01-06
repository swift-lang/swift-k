package org.globus.cog.monitor.guss;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class which stores all host names encountered when reading
 * logfiles.  Maps each one to a unique id number and eliminates
 * redundancy.  This is a singleton class; all methods are static, so
 * there's no way to instantiate it.
 * @version 1.3
 */
class HostNameHash
{
    /*Whenever we read in a new GFTPRecord, see if it src/dest goes with an
     * existing host...if not, add it to STATIC hashtable of hostnames.*/

    static private HashMap hostNames = new HashMap();
    //Keys are host names; values are corresponding arbitrary IDs.

    static private ArrayList namesInOrder = new ArrayList();
    /*To let us efficiently look up name by number.  Both looking up name from number and number from name must be efficient.*/

    /**
     *Looks up the ID number associated with a host name.
     *If the named host is not already in the hash, it will be added to the hash and an ID number
     *assigned to it.  This is the only way to add hosts.
     *@param hostName Name of the host to look up
     *@return the ID number
     */
    public static Integer nameToNumber(String hostName) {
	if (hostNames.containsKey(hostName)) {
	    return (Integer)hostNames.get(hostName);
	    //name in hash -- get id from hash
	}
	else {
	    Integer newID;
	    newID = new Integer(hostNames.size()); //so will start from 0.
	    hostNames.put(hostName, newID);
	    namesInOrder.add(hostName);
	    return newID;
	    //name not in hash -- add it, get id.
	}
    }

    /**
     *Checks whether the named host is in the hash
     *@param hostName Name of the host to look up
     *@return true if the hash contains this host, false otherwise.
     */
    public static boolean contains(String hostName) {
	return hostNames.containsKey(hostName);
    }

    /**
     *Looks up an ID number and returns the name of the host.
     *@param hostNumber the ID number to look up
     *@return If that ID number exists, the hostname associated with it; otherwise, the empty string.
     */
    public static String numberToName(int hostNumber) {
	
	if (hostNumber < namesInOrder.size()) {
	    return (String)namesInOrder.get(hostNumber);
	}
	else
	    return "";
    }
    
    /**
     *Gets the number of unique hosts in the hash.
     *@return number of hosts
     */
    public static int numHosts() {
	return hostNames.size();
    }
    
    /**
     *Lists the names of all hosts currently in the hash.
     *@return space-separated list of host names.
     */
    public static String listHostsInOrder() {
	int id;
	int max = namesInOrder.size();
	String returnMe = "";

	for (id = 0; id < max; id++) {
	    returnMe = returnMe + namesInOrder.get(id) + " ";
	}

	return returnMe;
    }
}
