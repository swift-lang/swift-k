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
 * Created on Jan 7, 2013
 */
package org.griphyn.vdl.util;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.karajan.scheduler.WeightedHost;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.TransformationCatalog;
import org.globus.swift.catalog.site.SiteCatalogParser;
import org.globus.swift.catalog.transformation.File;
import org.globus.swift.catalog.util.Profile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConvertConfig {
    
    private enum Version {
        V1, V2;
    }

    private ContactSet parseSites(String fileName) {
        SiteCatalogParser p = new SiteCatalogParser(fileName);
        try {
            Document doc = p.parse();
            return buildResources(doc);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse site catalog", e);
        }
    }
    
    private TransformationCatalog loadTC(String fileName) {
        return File.getNonSingletonInstance(fileName);
    }
    
    public void run(String sitesFile, String tcFile, String conf, PrintStream ps) {
        if (sitesFile == null && tcFile == null && conf == null) {
            throw new IllegalArgumentException("You must specify at " +
            		"least one file type to convert");
        }
        ContactSet sites = null;
        if (sitesFile != null) {
            sites = parseSites(sitesFile);
        }
        TransformationCatalog tc = null;
        if (tcFile != null) {
            tc = loadTC(tcFile);
        }
        
        generateConfig(sites, tc, ps);
        
        if (conf != null) {
            Properties props = new Properties();
            try {
                props.load(new FileReader(conf));
                generateConfig(props, ps);
            }
            catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }
        }
    }

    private void generateConfig(Properties props, PrintStream ps) {
        Set<String> processed = new HashSet<String>();
        for (String name : propOrder) {
            if (props.containsKey(name)) {
                prop(name, props.get(name), ps);
                processed.add(name);
            }
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String name = (String) e.getKey();
            if (!processed.contains(name)) {
                prop(name, props.get(name), ps);
            }
        }
    }

    private void prop(String name, Object value, PrintStream ps) {
        if (removedAttrs.contains(name)) {
                ps.println("# option removed: " + name);
                return;
        }
        if (name.equals("use.provider.staging")) {
            if (TypeUtil.toBoolean(value)) {
                ps.println("staging: \"local\"");
            }
            return;
        }
        if (name.equals("use.wrapper.staging")) {
            if (TypeUtil.toBoolean(value)) {
                ps.println("staging: \"wrapper\"");
            }
            return;
        }
        Attr a = props.get(name);
        if (a == null) {
            throw new RuntimeException("Unknown configuration property '" + name + "'");
        }
        ps.print(a.name + ": ");
        printValue(a.type, value, ps);
    }

    private void generateConfig(ContactSet sites, TransformationCatalog tc, PrintStream ps) {
        ps.println();
        
        if (sites != null) {
            for (BoundContact bc : sites.getContacts()) {
                // the default swift.conf contains a site called local,
                // so make sure it's overwritten
                if ("local".equals(bc.getName())) {
                    ps.println("site.local: null");
                }
                ps.println("site." + bc.getName() + " {");
                boolean hasFileService = false;
                boolean hasCoasterService = false;
                for (Map.Entry<BoundContact.TypeProviderPair, Service> e : bc.getServices().entrySet()) {
                    String provider = e.getKey().provider;
                    if (provider == null) {
                        continue;
                    }
                    if (e.getKey().type == Service.EXECUTION) {
                        ExecutionService es = (ExecutionService) e.getValue();
                        beginObject(1, "site.*.execution", ps);
                        writeValue(2, "site.*.execution.type", provider, ps);
                        if (es.getServiceContact() != null) {
                            writeValue(2, "site.*.execution.URL", es.getServiceContact().getContact(), ps);
                        }
                        if (es.getJobManager() != null) {
                            writeValue(2, "site.*.execution.jobManager", es.getJobManager(), ps);
                        }
                        if (provider.equals("coaster")) {
                            hasCoasterService = true;
                            generateCoasterServiceAttributes(bc, ps);
                        }
                        endObject(1, ps);
                        if (!provider.equals("coaster")) {
                            generateServiceAttributes(es, ps);
                        }
                    }
                    else if (e.getKey().type == Service.FILE_OPERATION) {
                        hasFileService = true;
                        beginObject(1, "site.*.filesystem", ps);
                        writeValue(2, "site.*.filesystem.type", provider, ps);
                        if (e.getValue().getServiceContact() != null) {
                            writeValue(2, "site.*.filesystem.URL", e.getValue().getServiceContact().getContact(), ps);
                        }
                        endObject(1, ps);
                    }
                }
                if (!hasFileService) {
                    writeValue(1, "site.*.staging", "local", ps);
                }
                if (bc.hasProperty("workdir")) {
                    writeValue(1, "site.*.workDirectory", bc.getProperty("workdir"), ps);
                }
                if (bc.hasProperty("scratch")) {
                    writeValue(1, "site.*.scratch", bc.getProperty("scratch"), ps);
                }
                generateSiteAttributes(bc.getProperties(), ps);
                if (tc != null) {
                    generateAppInfo("site.*.", bc.getName(), 
                        (String) bc.getProperty("globus:maxwalltime"), tc, hasCoasterService, 1, ps);
                }
                else {
                    if (bc.hasProperty("globus:maxwalltime")) {
                        ps.println("\tapp.ALL {");
                        writeValue(2, "site.*.app.*.maxWallTime", 
                            WallTime.normalize((String) bc.getProperty("globus:maxwalltime"), "hms"), ps);
                        ps.println("\t}");
                    }
                }
                ps.println("}\n");
            }
        }
        if (tc != null) {
            generateAppInfo("", "*", null, tc, false, 0, ps);
        }
    }
    
    private void writeValue(int level, String key, Object value, PrintStream ps) {
        if (!SwiftConfig.SCHEMA.isNameValid(key)) {
            throw new IllegalArgumentException("Invalid property: " + key);
        }
        SwiftConfigSchema.Info i = SwiftConfig.SCHEMA.getInfo(key);
        if (i == null) {
            SwiftConfig.SCHEMA.getInfo(key);
            throw new IllegalArgumentException("No type information found for: " + key);
        }
        i.type.check(key, value, i.loc);
        tabs(level, ps);
        ps.print(last(key));
        ps.print(": ");
        if (value instanceof String) {
            ps.println(checkSubst((String) value));
        }
        else {
            ps.println(value);
        }
    }

    private String checkSubst(String str) {
        int i1 = str.indexOf('{');
        int i2 = str.indexOf('}', i1 + 1);
        if (i1 != -1 && i2 != -1) {
            StringBuilder sb = new StringBuilder();
            // insert a dollar sign
            if (i1 != 0) {
                sb.append('"');
                sb.append(str.subSequence(0, i1));
                sb.append('"');
            }
            sb.append('$');
            sb.append(str.substring(i1, i2 + 1));
            if (i2 != str.length() - 1) {
                sb.append('"');
                sb.append(str.substring(i2 + 1));
                sb.append('"');
            }
            return sb.toString();
        }
        else {
            return "\"" + str + "\"";
        }
    }

    private void endObject(int level, PrintStream ps) {
        tabs(level, ps);
        ps.println("}");
    }

    private void beginObject(int level, String key, PrintStream ps) {
        if (!SwiftConfig.SCHEMA.isNameValid(key)) {
            throw new IllegalArgumentException("Invalid property: " + key);
        }
        tabs(level, ps);
        ps.print(last(key));
        ps.println(" {");
    }

    private String last(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private void generateAppInfo(String prefix, String host, String maxWallTime, 
            TransformationCatalog tc, boolean hasCoasterService, int level, PrintStream ps) {
        try {
            for (TCEntry e : tc.getTC()) {
                if (e.getResourceId().equals(host)) {
                    if (e.getLogicalName().equals("*")) {
                        tabs(level, ps);
                        ps.println("app.ALL {");
                    }
                    else {
                        tabs(level, ps);
                        ps.println("app." + e.getLogicalName() + " {");
                    }
                    
                    writeValue(level + 1, prefix + "app.*.executable", e.getPhysicalTransformation(), ps);
                    if (e.getProfiles() != null) {
                        generateAppProfiles(prefix + "app.*.", e, hasCoasterService, level, ps);
                    }
                    
                    if (maxWallTime != null) {
                        writeValue(level + 1, prefix + "app.*.maxWallTime", 
                            WallTime.normalize(maxWallTime, "hms"), ps);
                    }
                    tabs(level, ps);
                    ps.println("}\n");
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get TC data", e);
        }
    }

    private void generateAppProfiles(String prefix, TCEntry e, boolean hasCoasterService, int level, PrintStream ps) {
        StringBuilder options = new StringBuilder();
        for (Profile p : e.getProfiles()) {
            if (p.getProfileKey().startsWith("env.")) {
                tabs(level, ps);
                ps.println(p.getProfileKey() + ": \"" + p.getProfileValue() + "\"");
            }
            else if (p.getProfileKey().equalsIgnoreCase("maxwalltime")) {
                writeValue(level, prefix + "maxWallTime", p.getProfileValue(), ps);
            }
            else if (p.getProfileKey().equalsIgnoreCase("queue")) {
                if (!hasCoasterService) {
                    writeValue(level, prefix + "jobQueue", p.getProfileValue(), ps);
                }
            }
            else if (p.getProfileKey().equalsIgnoreCase("project")) {
                if (!hasCoasterService) {
                    tabs(level, ps);
                    writeValue(level, prefix + "jobProject", p.getProfileValue(), ps);
                }
            }
            else {
                if (options.length() != 0) {
                    options.append(", ");
                }
                else {
                    options.append("{");
                }
                options.append(p.getProfileKey() + ": \"" + p.getProfileValue() + "\"");
            }
        }
        if (options.length() != 0) {
            options.append("}");
            tabs(level + 1, ps);
            ps.println("options " + options.toString());
        }
    }

    private void tabs(int count, PrintStream ps) {
        for (int i = 0; i < count; i++) {
            ps.print('\t');
        }
    }

    private void generateSiteAttributes(Map<String, Object> properties, PrintStream ps) {
        if (properties.containsKey("sysinfo")) {
            writeValue(1, "site.*.OS", properties.get("sysinfo"), ps);
        }
        if (properties.containsKey("delaybase")) {
            writeValue(1, "site.*.delayBase", properties.get("delaybase"), ps);
        }
        if (properties.containsKey("initialscore")) {
            double jobThrottle = 2;
            if (properties.containsKey("jobthrottle")) {
                jobThrottle = TypeUtil.toDouble(properties.get("jobthrottle"));
            }
            double initialScore = TypeUtil.toDouble(properties.get("initialscore"));
            writeValue(1, "site.*.maxParallelTasks", (int) (jobThrottle * WeightedHost.T + 1), ps);
            writeValue(1, "site.*.initialParallelTasks", (int) (jobThrottle * WeightedHost.computeTScore(initialScore) + 1), ps);
        }
        if (properties.containsKey("maxsubmitrate")) {
            writeValue(1, "site.*.maxSubmitRate", properties.get("maxubmitrate"), ps);
        }
    }

    private enum AttrType {
        INT, FLOAT, STRING, BOOLEAN, TIME_FROM_SECONDS;
    }
    
    private static class Attr {
        public Attr(String k, AttrType t) {
            this.name = k;
            this.type = t;
        }
        public AttrType type;
        public String name;
    }
    
    private static final Set<String> removedAttrs;
    private static final Map<String, Attr> coasterAttrs;
    private static final Map<String, Attr> serviceAttrs;
    private static final Map<String, Attr> props;
    private static List<String> propOrder;
    
    static {
        removedAttrs = new HashSet<String>();
        removedAttrs.add("pgraph");
        removedAttrs.add("pgraph.graph.options");
        removedAttrs.add("pgraph.node.options");
        removedAttrs.add("clustering.enabled");
        removedAttrs.add("clustering.queue.delay");
        removedAttrs.add("clustering.min.time");
        removedAttrs.add("kickstart.enabled");
        removedAttrs.add("kickstart.always.transfer");
        removedAttrs.add("sites.file");
        removedAttrs.add("tc.file");
        
        coasterAttrs = new HashMap<String, Attr>();
        attr(coasterAttrs, "queue", "jobQueue", AttrType.STRING);
        attr(coasterAttrs, "project", "jobProject", AttrType.STRING);
        attr(coasterAttrs, "maxtime", "maxJobTime", AttrType.TIME_FROM_SECONDS); 
        attr(coasterAttrs, "reserve", AttrType.STRING);
        attr(coasterAttrs, "lowOverallocation", AttrType.INT);
        attr(coasterAttrs, "highOverallocation", AttrType.INT);
        attr(coasterAttrs, "slots", "maxJobs", AttrType.INT); 
        attr(coasterAttrs, "maxNodes", "maxNodesPerJob", AttrType.INT);
        attr(coasterAttrs, "overallocationDecayFactor", AttrType.FLOAT);
        attr(coasterAttrs, "internalHostname", AttrType.STRING);
        attr(coasterAttrs, "allocationStepSize", AttrType.FLOAT);
        attr(coasterAttrs, "nodeGranularity", AttrType.INT);
        attr(coasterAttrs, "remoteMonitorEnabled", AttrType.BOOLEAN);
        attr(coasterAttrs, "userHomeOverride", AttrType.STRING);
        attr(coasterAttrs, "workerLoggingLevel", AttrType.STRING);
        attr(coasterAttrs, "workerLoggingDirectory", AttrType.STRING);
        attr(coasterAttrs, "workerManager", AttrType.STRING);
        attr(coasterAttrs, "softImage", AttrType.STRING);
        attr(coasterAttrs, "jobsPerNode", "tasksPerNode", AttrType.INT);
        attr(coasterAttrs, "ppn", "jobOptions.ppn", AttrType.INT);
        
        serviceAttrs = new HashMap<String, Attr>();
        attr(serviceAttrs, "queue", "jobQueue", AttrType.STRING);
        attr(serviceAttrs, "project", "jobProject", AttrType.STRING);
        attr(serviceAttrs, "jobType", "jobType", AttrType.STRING);
                
        props = new HashMap<String, Attr>();
        propOrder = new ArrayList<String>();
        prop("hostname", "hostName", AttrType.STRING);
        prop("tcp.port.range", "TCPPortRange", AttrType.STRING);
        prop("lazy.errors", "lazyErrors", AttrType.BOOLEAN);
        prop("execution.retries", "executionRetries", AttrType.INT);
        prop("caching.algorithm", "cachingAlgorithm", AttrType.STRING);
        prop("throttle.submit", "jobSubmitThrottle", AttrType.INT);
        prop("throttle.host.submit", "hostJobSubmitThrottle", AttrType.INT);
        prop("throttle.transfers", "fileTransfersThrottle", AttrType.INT);
        prop("throttle.file.operations", "fileOperationsThrottle", AttrType.INT);
        prop("throttle.score.job.factor", "siteScoreThrottlingFactor", AttrType.INT);
        prop("sitedir.keep", "keepSiteDir", AttrType.BOOLEAN);
        prop("provenance.log", "logProvenance", AttrType.BOOLEAN);
        prop("replication.enabled", "replicationEnabled", AttrType.BOOLEAN);
        prop("replication.min.queue.time", "replicationMinQueueTime", AttrType.INT);
        prop("replication.limit", "replicationLimit", AttrType.INT);
        prop("status.mode", "statusMode", AttrType.STRING);
        prop("wrapper.parameter.mode", "wrapperParameterMode", AttrType.STRING);
        prop("wrapper.invocation.mode", "wrapperInvocationMode", AttrType.STRING);
        prop("cdm.broadcast.mode", "CDMBroadcastMode", AttrType.STRING);
        prop("provider.staging.pin.swiftfiles", "providerStagingPinSwiftFiles", AttrType.BOOLEAN);
        prop("ticker.date.format", "tickerDateFormat", AttrType.STRING);
        prop("ticker.prefix", "tickerPrefix", AttrType.STRING);
        prop("ticker.enabled", "tickerEnabled", AttrType.BOOLEAN);
        prop("file.gc.enabled", "fileGCEnabled", AttrType.BOOLEAN);
        prop("mapping.checker", "mappingCheckerEnabled", AttrType.BOOLEAN);
        prop("wrapperlog.always.transfer", "alwaysTransferWrapperLog", AttrType.BOOLEAN);
        prop("tracing.enabled", "tracingEnabled", AttrType.BOOLEAN);
        prop("wrapper.staging.local.server", "wrapperStagingLocalServer", AttrType.STRING);
        prop("foreach.max.threads", "maxForeachThreads", AttrType.INT);
    }
    
    
    private static void attr(Map<String, Attr> m, String k, AttrType t) {
        m.put("globus:" + k.toLowerCase(), new Attr(k, t));
    }
    
    private static void attr(Map<String, Attr> m, String k, String v, AttrType t) {
        m.put("globus:" + k.toLowerCase(), new Attr(v, t));
    }
    
    private static void prop(String k, AttrType t) {
        if (!SwiftConfig.SCHEMA.isNameValid(k)) {
            throw new Error("Invalid prop: '" + k + "'");
        }
        props.put(k, new Attr(k, t));
        propOrder.add(k);
    }
    
    private static void prop(String k, String v, AttrType t) {
        if (!SwiftConfig.SCHEMA.isNameValid(v)) {
            throw new Error("Invalid prop: '" + v + "'");
        }
        props.put(k, new Attr(v, t));
        propOrder.add(k);
    }

    private void generateCoasterServiceAttributes(BoundContact bc, PrintStream ps) {
        ps.println("\t\toptions {");
        for (String attr : bc.getProperties().keySet()) {
            Attr a = coasterAttrs.get(attr.toLowerCase());
            if (a != null) {
                ps.print("\t\t\t" + a.name + ": ");
                printValue(a.type, bc.getProperty(attr), ps);
            }
            else if (attr.equalsIgnoreCase("globus:providerAttributes")) {
                providerAttributes((String) bc.getProperty(attr), ps);
            }
            else if (attr.equalsIgnoreCase("globus:maxWallTime")) {
                // handled somewhere else
            }
            else {
                if (attr.startsWith("globus:")) {
                    ps.println("\t\t\t# Option ignored: " + attr + " = " + bc.getProperty(attr));
                }
            }
        }
        ps.println("\t\t}");
    }
    
    private void providerAttributes(String val, PrintStream ps) {
        ps.println("\t\t\tjobOptions {");
        String[] tokens = val.split("[;\n]");
        for (String token : tokens) {
            token = token.trim();
            if (token.length() > 0) {
                String[] t2 = token.split("=", 2);
                if (t2.length == 1) {
                    ps.println("\t\t\t\t" + t2[0] + ": true");
                }
                else {
                    ps.println("\t\t\t\t" + t2[0] + ": \"" + t2[1] + "\"");
                }
            }
        }
        
        ps.println("\t\t\t}");
    }

    private void printValue(AttrType type, Object value, PrintStream ps) {
        switch (type) {
            case INT:
            case FLOAT:
            case BOOLEAN:
                ps.println(value);
                break;
            case STRING:
                ps.println(checkSubst(String.valueOf(value)));
                break;
            case TIME_FROM_SECONDS:
                ps.println("\"" + WallTime.format("hms", TypeUtil.toInt(value)) + "\"");
                break;
        }
    }

    private void generateServiceAttributes(Service s, PrintStream ps) {
        for (String attr : s.getAttributeNames()) {
            Attr a = serviceAttrs.get(attr);
            if (a != null) {
                ps.print("\t\t" + a.name + ": ");
                switch (a.type) {
                    case INT:
                    case FLOAT:
                    case BOOLEAN:
                        ps.println(s.getAttribute(attr));
                        break;
                    case STRING:
                        ps.println("\"" + s.getAttribute(attr) + "\"");
                        break;
                }
            }
            else {
                if (attr.startsWith("globus.") && coasterAttrs.containsKey(attr)) {
                    throw new RuntimeException("Unrecognize profile: '" + attr + "'");
                }
            }
        }
    }

    private ContactSet buildResources(Document doc) {
        Node root = getRoot(doc);
        
        if (root.getLocalName().equals("config")) {
            return parse(root, Version.V1);
        }
        else if (root.getLocalName().equals("sites")) {
            return parse(root, Version.V1);
        }
        else {
            throw new IllegalArgumentException("Illegal sites file root node: " + root.getLocalName());
        }
    }    
    

    private ContactSet parse(Node config, Version v) {
        ContactSet cs = new ContactSet();
        NodeList pools = config.getChildNodes();
        for (int i = 0; i < pools.getLength(); i++) {
            Node n = pools.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    BoundContact bc = pool(n, v);
                    if (bc != null) {
                        cs.addContact(bc);
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException("Invalid site entry '" + poolName(n, v) + "': ", e);
                }
            }
        }
        return cs;
    }

    private String poolName(Node site, Version v) {
       if (site.getLocalName().equals("pool")) {
           return attr(site, "handle");
       }
       else if (site.getLocalName().equals("site")) {
           return attr(site, "name");
       }
       else {
           throw new IllegalArgumentException("Invalid node: " + site.getLocalName());
       }
    }

    private Node getRoot(Document doc) {
        NodeList l = doc.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return l.item(i);
            }
        }
        throw new IllegalArgumentException("Missing root element");
    }

    private BoundContact pool(Node n, Version v) throws InvalidProviderException, ProviderMethodException {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        String name = poolName(n, v);
        BoundContact bc = new BoundContact(name);
        
        String sysinfo = attr(n, "sysinfo", null);
        if (sysinfo != null) {
            bc.setProperty("sysinfo", sysinfo);
        }
        
        NodeList cs = n.getChildNodes();
        
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (v == Version.V1 && ctype.equals("gridftp")) {
                bc.addService(gridftp(c));
            }
            else if (v == Version.V1 && ctype.equals("jobmanager")) {
                bc.addService(jobmanager(c));
            }
            else if (ctype.equals("execution")) {
                bc.addService(execution(c));
            }
            else if (ctype.equals("filesystem")) {
                bc.addService(filesystem(c));
            }
            else if (ctype.equals("workdirectory")) {
                bc.setProperty("workdir", text(c));
            }
            else if (ctype.equals("scratch")) {
                bc.setProperty("scratch", text(c));
            }
            else if (ctype.equals("env")) {
                env(bc, c);
            }
            else if (ctype.equals("profile")) {
                profile(bc, c);
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }
        return bc;
    }

    private Service jobmanager(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider;
        String url = attr(n, "url");
        String major = attr(n, "major");
        if (url.equals("local://localhost")) {
            provider = "local";
        }
        else if (url.equals("pbs://localhost")) {
            provider = "pbs";
        }
        else if ("2".equals(major)) {
            provider = "gt2";
        }
        else if ("4".equals(major)) {
            provider = "gt4";
        }
        else {
            throw new IllegalArgumentException("Unknown job manager version: " + major + ", url = '" + url + "'");
        }
        
        ServiceContact contact = new ServiceContactImpl(url);
            return new ServiceImpl(provider, Service.EXECUTION, 
                contact, AbstractionFactory.newSecurityContext(provider, contact));
    }

    private Service gridftp(Node n) throws InvalidProviderException, ProviderMethodException {
        String url = attr(n, "url");
        if (url.equals("local://localhost")) {
            return new ServiceImpl("local", Service.FILE_OPERATION, new ServiceContactImpl("localhost"), null);
        }
        else {
            ServiceContact contact = new ServiceContactImpl(url);
            return new ServiceImpl("gsiftp", Service.FILE_OPERATION, 
                contact, AbstractionFactory.newSecurityContext("gsiftp", contact));
        }
    }

    private Service execution(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider = attr(n, "provider");
        String url = attr(n, "url", null);
        String jobManager = attr(n, "jobManager", null);
        if (jobManager == null) {
            jobManager = attr(n, "jobmanager", null);
        }
        
        ExecutionService s = new ExecutionServiceImpl();
        s.setProvider(provider);
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
        
        if (jobManager != null) {
            s.setJobManager(jobManager);
        }
        
        return s;
    }

    private Service filesystem(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider = attr(n, "provider");
        String url = attr(n, "url", null);
        
        Service s = new ServiceImpl();
        s.setType(Service.FILE_OPERATION);
        s.setProvider(provider);
        
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
        
        return s;
    }

    private void env(BoundContact bc, Node n) {
        String key = attr(n, "key");
        String value = text(n);
        
        bc.setProperty("env:" + key, value);
    }

    private void profile(BoundContact bc, Node n) {
        String ns = attr(n, "namespace");
        String key = attr(n, "key");
        String value = text(n);
        
        if (value == null) {
            throw new IllegalArgumentException("No value for profile " + ns + ":" + key);
        }
        if (ns.equals("karajan")) {
            bc.setProperty(key.toLowerCase(), value);
        }
        else {
            bc.setProperty(ns + ":" + key.toLowerCase(), value);
        }
    }

    private String text(Node n) {
        if (n.getFirstChild() != null) {
            return n.getFirstChild().getNodeValue();
        }
        else {
            return null;
        }
    }

    private String attr(Node n, String name) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(name);
            if (attr == null) {
                throw new IllegalArgumentException("Missing " + name);
            }
            else {
                return attr.getNodeValue();
            }
        }
        else {
            throw new IllegalArgumentException("Missing " + name);
        }
    }
    
    private String attr(Node n, String name, String defVal) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(name);
            if (attr == null) {
                return defVal;
            }
            else {
                return attr.getNodeValue();
            }
        }
        else {
            return defVal;
        }
    }
    
    public static void main(String[] args) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("swift-convert-config");
        ap.addFlag("help", "Displays usage information");
        ap.addOption("sites.file", "Specifies a sites file to convert", 
            "file", ArgumentParser.OPTIONAL);
        ap.addOption("tc.file", "Specifies a tc.data file to convert", 
            "file", ArgumentParser.OPTIONAL);
        ap.addOption("config", "Specifies an old Swift configuration file to convert", 
            "file", ArgumentParser.OPTIONAL);
        ap.addOption("out", "Indicates that the output should go to a file instead of standard output", 
            "file", ArgumentParser.OPTIONAL);
        
        try {
            ap.parse(args);
        }
        catch (ArgumentParserException e) {
            System.out.println("Error parsing command line arguments: " + e.getMessage());
            usage(ap);
            System.exit(1);
        }
        if (ap.isPresent("help")) {
            usage(ap);
            System.exit(0);
        }
        ConvertConfig cc = new ConvertConfig();
        try {
            PrintStream ps = System.out;
            if (ap.isPresent("out")) {
                ps = new PrintStream(new FileOutputStream(ap.getStringValue("out")));
            }
            cc.run(ap.getStringValue("sites.file", null), 
                ap.getStringValue("tc.file", null), ap.getStringValue("config", null), ps);
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Failed to convert configuration: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void usage(ArgumentParser ap) {
        System.out.println(
            "Converts an existing set of sites.xml, tc.data, and swift.properties \n" +
            "files (at least one of which needs to be specified) to a unified Swift\n" +
            "configuration file.\n");
        ap.usage();
    }
}
