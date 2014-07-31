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
