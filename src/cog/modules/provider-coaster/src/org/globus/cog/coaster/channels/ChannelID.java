//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2005
 */
package org.globus.cog.coaster.channels;

import java.rmi.server.UID;

public class ChannelID {
	private String localID, remoteID, uniqueID;
	private boolean client;

	public String getRemoteID() {
		return remoteID;
	}

	public void setRemoteID(String remoteID) {
		this.remoteID = remoteID;
		uniqueID = null;
	}

	public String getLocalID() {
		return localID;
	}

	public void setLocalID(String localID) {
		this.localID = localID;
		uniqueID = null;
	}

	public String getUniqueID() {
		if (uniqueID == null) {
			uniqueID = localID + "-" + remoteID;
		}
		return uniqueID;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ChannelID) {
			ChannelID other = (ChannelID) obj;
			return getUniqueID().equals(other.getUniqueID()) && client == other.client;
		}
		return false;
	}

	public int hashCode() {
		return getUniqueID().hashCode() + (client ? 0 : 1);
	}

	public String toString() {
		return getUniqueID() + (client ? "C" : "S");
	}

	public void setClient(boolean client) {
		this.client = client;
	}
	
	public static String newUID() {
		return "u" + new UID().toString().replace(':', '-');
	}
}
