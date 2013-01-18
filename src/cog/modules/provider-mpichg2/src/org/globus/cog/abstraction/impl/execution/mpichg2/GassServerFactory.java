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