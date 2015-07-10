/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on Jan 30, 2015
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.util.http.AbstractHTTPServer;
import org.globus.common.CoGProperties;

public class SimpleHTTPServer extends AbstractHTTPServer {
    public static final Logger logger = Logger.getLogger(SimpleHTTPServer.class);

    private ClassLoader loader = SimpleHTTPServer.class.getClassLoader();
    private final String webDir;
    
    public SimpleHTTPServer(int port, String password, String webDir) {
        super("HTTPMonitor", port, password);
        this.webDir = webDir;
    }
    
    
    
    @Override
    protected ServerSocketChannel openChannel() throws IOException {
        return channel = ServerSocketChannel.open();
    }



    @Override
    protected void bind(ServerSocket socket, int port) throws IOException {
        channel.socket().bind(new InetSocketAddress(port));
    }
    
    public String getURL() {
        if (channel == null) {
            throw new IllegalThreadStateException(
                    "Server has not been started yet");
        }
        else {
            ServerSocket socket = channel.socket();
            if (CoGProperties.getDefault().getHostName() != null) {
                return "http://" + CoGProperties.getDefault().getHostName()
                    + ":" + socket.getLocalPort();
            }
            else {
                return "http://localhost:" + socket.getLocalPort();
            }
        }
    }
    
    public boolean exists(String file) {
        URL url = loader.getResource(webDir + file);
        return url != null;
    }
    
    protected DataLink getDataLink(String file, Map<String, String> params) {
        URL url = loader.getResource(webDir + file);
        if (url == null) {
            return null;
        }
        else {
            return new DataLink(url);
        }
    }
}
