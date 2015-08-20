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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractBlockWorkerManager;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractSettings;
import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.coaster.service.job.manager.Node;
import org.globus.cog.abstraction.coaster.service.job.manager.QueueProcessor;
import org.globus.cog.abstraction.coaster.service.job.manager.Time;
import org.globus.cog.coaster.channels.PerformanceDiagnosticInputStream;
import org.globus.cog.coaster.channels.PerformanceDiagnosticOutputStream;
import org.globus.cog.util.http.AbstractHTTPServer;
import org.globus.cog.util.json.JSONEncoder;

public class SettingsServer extends AbstractHTTPServer {
    public static final Logger logger = Logger.getLogger(SettingsServer.class);
    
    private static final Set<String> valid = new HashSet<String>();
    static {
        valid.add("/get");
        valid.add("/set");
        valid.add("/list");
        valid.add("/getAll");
        valid.add("/wmInfo");
        valid.add("/index.html");
    }
    
    private AbstractSettings settings;
    private AbstractBlockWorkerManager wm;
    
    public SettingsServer(QueueProcessor queueProcessor, int port) {
        super("Settings", port, null);
        this.settings = queueProcessor.getSettings();
        if (queueProcessor instanceof AbstractBlockWorkerManager) {
            this.wm = (AbstractBlockWorkerManager) queueProcessor;
        }
    }
    
    protected String getAllowedOrigin() {
        return "*";
    }

    @Override
    protected boolean exists(String url) {
        return valid.contains(url);
    }

    @Override
    protected DataLink getDataLink(String url, Map<String, String> params) {
        if (url.equals("/get")) {
            return new DataLink(get(params.get("name")), "text/json");
        }
        else if (url.equals("/set")) {
            return new DataLink(set(params.get("name"), params.get("value")), "text/json");
        }
        else if (url.equals("/list")) {
            return new DataLink(list(), "text/json");
        }
        else if (url.equals("/getAll")) {
            return new DataLink(getAll(), "text/json");
        }
        else if (url.equals("/wmInfo")) {
            return new DataLink(getBlockInfo(), "text/json");
        }
        else {
            return new DataLink(makeIndexPage(), "text/html");
        } 
    }

    private ByteBuffer getBlockInfo() {
        JSONEncoder e = new JSONEncoder();
        e.beginMap();
        if (wm == null) {
            e.writeMapItem("supported", false);
        }
        else {
            e.writeMapItem("supported", true);
            Map<String, Object> wmData = wm.getMonitoringData();
            e.writeMapItem("time", Time.now().getMilliseconds());
            e.writeMapKey("wmdata");
            e.writeMap(wmData);
            e.writeMapKey("runtime");
            writeRuntimeInfo(e);
            List<Block> blocks = wm.getAllBlocks();
            e.writeMapKey("blocks");
            e.beginArray();
            long idle = 0;
            long busy = 0;
            for (Block b : blocks) {
                writeBlock(e, b);
                idle += b.getIdleTime();
                busy += b.getBusyTime();
            }
            e.endArray();
            if (idle + busy > 0) {
                e.writeMapItem("utilization", ((double) busy) / (idle + busy));
            }
            else {
                e.writeMapItem("utilization", 0.0);
            }
        }
        e.endMap();
        return ByteBuffer.wrap(e.toString().getBytes());
    }

    private void writeRuntimeInfo(JSONEncoder e) {
        Runtime r = Runtime.getRuntime();
        e.beginMap();
        e.writeMapItem("maxHeap", r.maxMemory());
        e.writeMapItem("crtHeap", r.totalMemory());
        e.writeMapItem("usedHeap", r.totalMemory() - r.freeMemory());
        e.writeMapItem("freeHeap", r.freeMemory());
        e.writeMapItem("readBytesTotal", PerformanceDiagnosticInputStream.getTotal());
        e.writeMapItem("readCurrentRate", PerformanceDiagnosticInputStream.getCurrentRate());
        e.writeMapItem("readAverageRate", PerformanceDiagnosticInputStream.getAverageRate());
        e.writeMapItem("writeBytesTotal", PerformanceDiagnosticOutputStream.getTotal());
        e.writeMapItem("writeCurrentRate", PerformanceDiagnosticOutputStream.getCurrentRate());
        e.writeMapItem("writeAverageRate", PerformanceDiagnosticOutputStream.getAverageRate());
        e.endMap();
    }

    private void writeBlock(JSONEncoder e, Block b) {
        e.beginArrayItem();
        e.beginMap();
        e.writeMapItem("id", b.getId());
        e.writeMapItem("running", b.isRunning());
        e.writeMapItem("workerCount", b.getActiveWorkerCount());
        e.writeMapItem("jobsCompleted", b.getDoneJobCount());
        e.writeMapItem("walltime", b.getWalltime().getMilliseconds());
        e.writeMapItem("utilization", b.getUtilization());
        writeTime(e, "creationTime", b.getCreationTime());
        writeTime(e, "startTime", b.getStartTime());
        writeTime(e, "shutdownTime", b.getShutdownTime());
        writeTime(e, "terminationTime", b.getTerminationTime());
        writeTime(e, "endTime", b.getEndTime());
        e.writeMapKey("nodes");
        e.beginArray();
        Collection<Node> nodes = b.getNodes();
        for (Node node : nodes) {
            writeNode(e, node);
        }
        e.endArray();
        e.writeMapKey("cpus");
        e.beginArray();
        Collection<Cpu> cpus = b.getCpusSnapshot();
        for (Cpu cpu : cpus) {
            e.beginArrayItem();
            writeCpu(e, cpu);
            e.endArrayItem();
        }
        e.endArray();
        e.endMap();
        e.endArrayItem();
    }

    private void writeNode(JSONEncoder e, Node node) {
        e.beginMap();
        e.writeMapItem("id", node.getId());
        writeTime(e, "startTime", node.getStartTime());
        e.endMap();
    }

    private void writeCpu(JSONEncoder e, Cpu cpu) {
        e.beginMap();
        e.writeMapItem("id", cpu.getId());
        e.writeMapItem("quality", cpu.getQuality());
        Job job = cpu.getRunning();
        if (job == null) {
            e.writeMapItem("job", null);
        }
        else {
            e.writeMapKey("job");
            writeJob(e, job);
        }
        e.endMap();
    }

    private void writeJob(JSONEncoder e, Job job) {
        e.beginMap();
        e.writeMapItem("id", job.getTask().getIdentity().toString());
        writeTime(e, "startTime", job.getStartTime());
        writeTime(e, "endTime", job.getEndTime());
        e.writeMapItem("walltime", job.getMaxWallTime().getMilliseconds());
        e.endMap();
    }

    private void writeTime(JSONEncoder e, String key, Time t) {
        if (t == null) {
            e.writeMapItem(key, null);
        }
        else {
            e.writeMapItem(key, t.getMilliseconds());
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
    
    private ByteBuffer getAll() {
        JSONEncoder e = new JSONEncoder();
        e.beginMap();
        e.writeMapItem("error", false);
        e.writeMapItem("errorMessage", null);
        e.writeMapKey("result");
        e.beginMap();
        for (String name : settings.getNames()) {
            try {
                e.writeMapItem(name, settings.get(name));
            }
            catch (Exception ex) {
                e.writeMapItem(name, "<error>");
            }
        }
        e.endMap();
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
