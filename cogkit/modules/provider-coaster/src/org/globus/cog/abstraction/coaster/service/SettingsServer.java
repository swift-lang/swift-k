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
package org.globus.cog.abstraction.coaster.service;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractSettings;
import org.globus.cog.util.http.AbstractHTTPServer;
import org.globus.cog.util.json.JSONEncoder;

public class SettingsServer extends AbstractHTTPServer {
    public static final Logger logger = Logger.getLogger(SettingsServer.class);
    
    private static final Set<String> valid = new HashSet<String>();
    static {
        valid.add("/get");
        valid.add("/set");
        valid.add("/list");
        valid.add("/index.html");
    }
    
    private AbstractSettings settings;
    
    public SettingsServer(AbstractSettings settings, int port) {
        super("Settings", port, null);
        this.settings = settings;
    }

    @Override
    protected boolean exists(String url) {
        return valid.contains(url);
    }

    @Override
    protected DataLink getDataLink(String url, Map<String, String> params) {
        if (url.equals("/get")) {
            return new DataLink(get(params.get("name")), "application/json");
        }
        else if (url.equals("/set")) {
            return new DataLink(set(params.get("name"), params.get("value")), "application/json");
        }
        else if (url.equals("/list")) {
            return new DataLink(list(), "application/json");
        }
        else {
            return new DataLink(makeIndexPage(), "text/html");
        } 
    }

    private ByteBuffer list() {
        JSONEncoder e = new JSONEncoder();
        e.beginMap();
        e.writeMapItem("error", false);
        e.writeMapItem("errorMessage", null);
        e.writeMapKey("result");
        e.beginArray();
        for (String name : settings.getNames()) {
            e.writeArrayItem(name);
        }
        e.endArray();
        e.endMap();
        return ByteBuffer.wrap(e.toString().getBytes());
    }

    private ByteBuffer set(String name, String value) {
        JSONEncoder e = new JSONEncoder();
        e.beginMap();
        try {
            settings.set(name, value);
            e.writeMapItem("error", false);
            e.writeMapItem("errorMessage", null);
        }
        catch (Exception ex) {
            e.writeMapItem("error", true);
            e.writeMapItem("errorMessage", ex.getMessage());
        }
        e.endMap();
        return ByteBuffer.wrap(e.toString().getBytes());
    }

    private ByteBuffer get(String name) {
        boolean error = false;
        Object value = null;
        String errorMessage = null;
        try {
            value = settings.get(name);
            error = false;
        }
        catch (Exception ex) {
            error = true;
            errorMessage = ex.getMessage();
        }
        
        JSONEncoder e = new JSONEncoder();
        e.beginMap();
        e.writeMapItem("success", !error);
        e.writeMapItem("errorMessage", errorMessage);
        e.writeMapItem("result", value);
        e.endMap();
        return ByteBuffer.wrap(e.toString().getBytes());
    }

    private ByteBuffer makeIndexPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Coaster Service Settings</title></head><body>");
        for (String name : settings.getNames()) {
            sb.append(name);
            sb.append(": ");
            try {
                sb.append(settings.get(name));
            }
            catch (Exception e) {
                sb.append("&lt;error&gt;");
            }
            sb.append("<br/>");
        }
        sb.append("</body></html>");
        return ByteBuffer.wrap(sb.toString().getBytes());
    }
}
