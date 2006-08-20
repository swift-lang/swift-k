// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2ft;

import java.util.HashMap;
import java.util.Iterator;

import org.globus.cog.abstraction.impl.execution.gt2.GassServerException;
import org.globus.common.CoGProperties;
import org.globus.io.gass.server.GassServer;
import org.ietf.jgss.GSSCredential;

public class GassServerFactory {
    private static HashMap mapping = new HashMap();
    private static String cogIP = CoGProperties.getDefault().getIPAddress();

    public static GassServer getGassServer(GSSCredential credential, int port)
            throws GassServerException {
        if (!GassServerFactory.cogIP.equalsIgnoreCase(CoGProperties
                .getDefault().getIPAddress())) {
            GassServerFactory.cogIP = CoGProperties.getDefault().getIPAddress();
            shutdownGassServers();
        }
        if (mapping.containsKey(credential)) {
            return (GassServer) mapping.get(credential);
        } else {
            GassServer gassServer = null;
            try {
                gassServer = new GassServer(credential, port);
                gassServer.setOptions(GassServer.STDERR_ENABLE
                        | GassServer.STDOUT_ENABLE | GassServer.READ_ENABLE
                        | GassServer.WRITE_ENABLE
                        | GassServer.CLIENT_SHUTDOWN_ENABLE);
            } catch (Exception e) {
                throw new GassServerException("Cannot start a gass server", e);
            }
            mapping.put(credential, gassServer);
            return gassServer;
        }
    }

    public static void shutdownGassServers() {
        Iterator iterator = mapping.values().iterator();
        while (iterator.hasNext()) {
            GassServer gs = (GassServer) iterator.next();
            gs.shutdown();
        }
        mapping.clear();
    }
}