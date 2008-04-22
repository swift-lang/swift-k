//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import org.globus.net.PortRange;

public class PortManager {
    private static PortManager portManager;

    public synchronized static PortManager getDefault() {
        if (portManager == null) {
            portManager = new PortManager();
        }
        return portManager;
    }

    private PortRange portRange;

    protected PortManager() {
        portRange = PortRange.getTcpInstance();
    }

    public ServerSocketChannel openServerSocketChannel() throws IOException {
        ServerSocketChannel s = ServerSocketChannel.open();
        bind(s.socket());
        return s;
    }

    public void close(ServerSocketChannel s) throws IOException {
        s.close();
        portRange.free(s.socket().getLocalPort());
    }

    private void bind(ServerSocket socket) throws IOException {
        int crt = 0;
        if (portRange.isEnabled()) {
            while (true) {
                crt = portRange.getFreePort(crt);

                try {
                    socket.bind(new InetSocketAddress(crt));

                    portRange.setUsed(crt);
                    return;
                }
                catch (IOException e) {
                    crt++;
                }
            }
        }
        else {
            socket.bind(null);
        }
    }
}
