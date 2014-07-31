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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.mpichg2;

import java.util.HashMap;
import java.util.Iterator;

import org.globus.common.CoGProperties;
import org.globus.io.gass.server.GassServer;
import org.ietf.jgss.GSSCredential;

public class GassServerFactory {
	private static HashMap mapping = new HashMap();
	private static String cogIP = CoGProperties.getDefault().getIPAddress();

	public static GassServer getGassServer(GSSCredential credential) throws GassServerException {
		if (GassServerFactory.cogIP == null) {
			if (CoGProperties.getDefault().getIPAddress() == null) {
				throw new GassServerException(
						"Could not determine this host's IP address. Please set an IP address in cog.properties");
			}
			else {
				GassServerFactory.cogIP = CoGProperties.getDefault().getIPAddress();
			}
		}
		else if (!GassServerFactory.cogIP.equalsIgnoreCase(CoGProperties.getDefault().getIPAddress())) {
			GassServerFactory.cogIP = CoGProperties.getDefault().getIPAddress();
			shutdownGassServers();
		}
		if (mapping.containsKey(credential)) {
			return (GassServer) mapping.get(credential);
		}
		else {
			GassServer gassServer = null;
			try {
				gassServer = new GassServer(credential, 0);
			}
			catch (Exception e) {
				throw new GassServerException("Cannot start a gass server", e);
			}
			mapping.put(credential, gassServer);
			return gassServer;
		}
	}

	private static void shutdownGassServers() {
		Iterator iterator = mapping.values().iterator();
		while (iterator.hasNext()) {
			GassServer gs = (GassServer) iterator.next();
			gs.shutdown();
		}
		mapping.clear();
	}
}