/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
