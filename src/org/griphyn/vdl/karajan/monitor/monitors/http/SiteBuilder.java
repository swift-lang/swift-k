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
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.util.ConfigTree;
import org.griphyn.vdl.util.ConfigTree.Node;
import org.griphyn.vdl.util.SwiftConfigSchema;
import org.griphyn.vdl.util.SwiftConfigSchema.Info;

public class SiteBuilder extends SimpleHTTPServer {
    public static final Logger logger = Logger.getLogger(SiteBuilder.class);

    public static final String SCHEMA_NAME = "/swift.conf.schema.json";
    public static final String WEB_DIR = "sitebuilder";
    private SwiftConfigSchema schema;
        
    public SiteBuilder(int port) {
        super(port, null, WEB_DIR);
        schema = new SwiftConfigSchema();
    }
    
    
    
    @Override
    public boolean exists(String file) {
        if (file.equals(SCHEMA_NAME)) {
            return true;
        }
        else {
            return super.exists(file);
        }
    }

    @Override
    protected DataLink getDataLink(String file, Map<String, String> params) {
        if (file.equals(SCHEMA_NAME)) {
            ByteBuffer buf = encodeSchema();
            return new DataLink(buf);
        }
        else {
            return super.getDataLink(file, params);
        }
    }
    
    private void encodeNodes(JSONEncoder enc, Collection<Map.Entry<String, Node<Info>>> s) {
        for (Map.Entry<String, Node<Info>> e : s) {
            enc.writeMapKey(e.getKey());
            enc.beginMap();
            if (e.getValue().isLeaf()) {
                encodeInfo(enc, e.getValue().get());
            }
            else {
                encodeNodes(enc, e.getValue().entrySet());
            }
            enc.endMap();
        }
    }
    
    private void encodeInfo(JSONEncoder enc, Info info) {
        enc.writeMapKey("type");
        if (info.typeSpec == null) {
            enc.write("?");
        }
        else {
            enc.write(info.typeSpec);
        }
        enc.writeMapKey("value");
        enc.write(info.value);
        enc.writeMapKey("optional");
        enc.write(info.optional);
        enc.writeMapKey("doc");
        enc.write(info.doc);
    }

    private ByteBuffer encodeSchema() {
        JSONEncoder enc = new JSONEncoder();
        ConfigTree<Info> tree = schema.getInfoTree();
        enc.beginMap();
        encodeNodes(enc, tree.entrySet());
        enc.endMap();
        return ByteBuffer.wrap(enc.toString().getBytes());
    }

    public void start() throws IOException {
        super.start();
        System.out.println("HTTP server started. URL is " + getURL());
    }
    
    public static void main(String[] args) {
        try {
            SiteBuilder sb = new SiteBuilder(3000);
            sb.start();
            while (true) {
                Thread.sleep(1000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
