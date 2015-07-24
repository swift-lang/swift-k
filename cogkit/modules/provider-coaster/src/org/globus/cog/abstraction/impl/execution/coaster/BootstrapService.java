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
 * Created on Jan 17, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Bootstrap;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Digester;
import org.globus.cog.util.http.AbstractHTTPServer;
import org.globus.common.CoGProperties;

public class BootstrapService extends AbstractHTTPServer {
    public static final Logger logger = Logger.getLogger(BootstrapService.class);

    private String webDir;
    private Set<String> valid;
    private Map<String,String> checksums;

    public BootstrapService() {
        super("Coaster Bootstrap", 0, null);
        initWebDir();
        initList();
    }

    private void initWebDir() {
        webDir = System.getProperty("coaster.bootstrap.service.web.dir");
        if (webDir == null) {
            webDir = System.getProperty("COG_INSTALL_PATH");
            if (webDir == null) {
                throw new IllegalArgumentException(
                        "None of 'coaster.bootstrap.service.web.dir' and 'COG_INSTALL_PATH' are set");
            }
            webDir = webDir + File.separator + "lib";
        }
    }

    private void initList() {
        valid = new HashSet<String>();
        valid.add("/" + ServiceManager.BOOTSTRAP_JAR);
        valid.add("/" + Bootstrap.BOOTSTRAP_LIST);
        valid.add("/index.html");
        checksums = new HashMap<String,String>();
        loadList();
    }

    private void loadList() {
        URL url = BootstrapService.class.getClassLoader().getResource(
                Bootstrap.BOOTSTRAP_LIST);
        if (url == null) {
            throw new RuntimeException(Bootstrap.BOOTSTRAP_LIST
                    + " not found in classpath");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            String line = br.readLine();
            while (line != null) {
                String[] d = line.split("\\s+");
                if (d.length != 2) {
                    throw new RuntimeException(
                            "Invalid line in package list: " + line);
                }
                if (!exists(d[0])) {
                    throw new RuntimeException(
                            "Could not find a file in the bootstrap list: "
                                    + d[0]);
                }
                valid.add("/" + d[0]);
                checksums.put("/" + d[0], d[1]);
                line = br.readLine();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not processs package list: "
                    + e.getMessage(), e);
        }
    }
    
    
    
    protected boolean exists(String name) {
        return getFile(name).exists();
    }

    protected synchronized String getMD5(File f) {
        String name = f.getName();
        String md5 = (String) checksums.get(name);
        if (md5 == null) {
            try {
                md5 = Digester.computeMD5(f);
            }
            catch (Exception e) {
                logger.warn("Could not compute checksum of " + f, e);
                return null;
            }
            checksums.put(name, md5);
        }
        return md5;
    }

    @Override
    protected String translateURL(String url) {
        if (url.equals("/")) {
            return "/index.html";
        }
        else if (url.equals("/list")) {
            return "/" + Bootstrap.BOOTSTRAP_LIST;
        }
        else {
            return url;
        }
    }

    @Override
    protected ServerSocketChannel openChannel() throws IOException {
        ServerSocketChannel channel = PortManager.getDefault().openServerSocketChannel();
        return channel;
    }
    
    @Override
    protected void bind(ServerSocket socket, int port) throws IOException {
        // bound by port manager
    }

    @Override
    public void start() throws IOException {
        super.start();
        logger.info("Socket bound. URL is " + getURL());
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
    
    

    @Override
    protected DataLink getDataLink(String url, Map<String, String> params) {
        String coasterId = params.get("serviceId");
        if (coasterId != null) {
            ServiceManager.getDefault().serviceIsActive(coasterId);
        }
        return new DataLink(getFile(url));
    }

    public File getFile(String name) {
        return new File(webDir + File.separator + name);
    }
}
