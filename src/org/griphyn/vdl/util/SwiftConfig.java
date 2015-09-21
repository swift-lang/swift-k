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
 * Created on Jul 5, 2014
 */
package org.griphyn.vdl.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;
import org.globus.swift.catalog.site.SwiftContactSet;
import org.globus.swift.catalog.types.SysInfo;
import org.griphyn.vdl.util.ConfigTree.Node;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigIncludeContext;
import com.typesafe.config.ConfigIncluder;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

public class SwiftConfig implements Cloneable {
    public static final Logger logger = Logger.getLogger(SwiftConfig.class);
    
    public static final boolean CHECK_DYNAMIC_NAMES = true;
    public static final boolean BUILD_CHECK = true;
    
    public static String DIST_CONF;
    public static String SITE_CONF;
    public static String USER_CONF;
    
    public enum Key {
        DM_CHECKER("mappingCheckerEnabled"),
        PROVENANCE_LOG("logProvenance"),
        FILE_GC_ENABLED("fileGCEnabled"),
        TICKER_ENABLED("tickerEnabled"),
        TICKER_DATE_FORMAT("tickerDateFormat"),
        TICKER_PREFIX("tickerPrefix"),
        TRACING_ENABLED("tracingEnabled"),
        FOREACH_MAX_THREADS("maxForeachThreads"),
        CACHING_ALGORITHM("cachingAlgorithm"), 
        REPLICATION_ENABLED("replicationEnabled"), 
        WRAPPER_STAGING_LOCAL_SERVER("wrapperStagingLocalServer"), 
        REPLICATION_MIN_QUEUE_TIME("replicationMinQueueTime"), 
        REPLICATION_LIMIT("replicationLimit"), 
        WRAPPER_INVOCATION_MODE("wrapperInvocationMode"), 
        CDM_BROADCAST_MODE("CDMBroadcastMode"), 
        CMD_FILE("CDMFile");
        
        public String propName;
        private Key(String propName) {
            this.propName = propName;
        }
    }
    
    public static SwiftConfigSchema SCHEMA;
    
    static {
        SCHEMA = new SwiftConfigSchema();
        
        String swiftHome = System.getProperty("swift.home");
        if (swiftHome == null) {
            swiftHome = System.getProperty("swift.home");
            if (swiftHome == null) {
                throw new IllegalStateException("swift.home is not set");
            }
        }
        
        DIST_CONF = makePath(swiftHome, "etc", "swift.conf");
        
        if (System.getenv("SWIFT_SITE_CONF") != null) {
            SITE_CONF = System.getenv("SWIFT_SITE_CONF");
        }
        
        File userConf = new File(makePath(System.getProperty("user.home"), ".swift", "swift.conf"));
        if (userConf.exists()) {
            USER_CONF = userConf.getAbsolutePath();
        }
        
        // check keys against schema
        for (Key k : Key.values()) {
            if (!SCHEMA.isNameValid(k.propName)) {
                throw new IllegalArgumentException("Invalid property name for config key '" + k + "': " + k.propName);
            }
        }
    }
    
    private static class KVPair {
        public final String key;
        public final String value;
        
        public KVPair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public static class ValueLocationPair {
        public final Object value;
        public final ConfigOrigin loc;
        
        public ValueLocationPair(Object value, ConfigOrigin loc) {
            this.value = value;
            this.loc = loc;
        }
    }
    
    private static class IncluderWrapper implements ConfigIncluder {
        private final ConfigIncluder d;
        private final List<String> loadedFiles;
        private final List<String> loadedFileIndices;
        private int index;
        
        public IncluderWrapper(ConfigIncluder d, List<String> loadedFiles, List<String> loadedFileIndices) {
            this.d = d;
            this.loadedFiles = loadedFiles;
            this.loadedFileIndices = loadedFileIndices;
        }

        @Override
        public ConfigIncluder withFallback(ConfigIncluder fallback) {
            return this;
        }

        @Override
        public ConfigObject include(ConfigIncludeContext context, String what) {
            int b = what.indexOf("${");
            while (b != -1) {
                int e = what.indexOf("}", b);
                String var = what.substring(b + 2, e);
                what = what.substring(0, b) + resolve(var) + what.substring(e + 1);
                b = what.indexOf("${");
            }
            loadedFiles.add(new File(what).getAbsolutePath());
            loadedFileIndices.add(String.valueOf(++index));
            return ConfigFactory.parseFile(new File(what)).root();
        }

        private String resolve(String var) {
            String v = null;
            if (var.startsWith("env.")) {
                v = System.getenv(var.substring(4));
            }
            else {
                v = System.getProperty(var);
            }
            if (v == null) {
                throw new IllegalArgumentException("No such system property or environment variable: '" + var + "'");
            }
            return v;
        }
    }

    private static String makePath(String... els) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < els.length; i++) {
            if (i != 0 && (sb.charAt(sb.length() - 1) != File.separatorChar)) {
                sb.append(File.separator);
            }
            sb.append(els[i]);
        }
        return sb.toString();
    }
    
    public static List<String> splitConfigSearchPath(String path) {
        if (path == null) {
            return null;
        }
        else {
            return Arrays.asList(path.split(File.pathSeparator));
        }
    }

    public static SwiftConfig load(String cmdLineConfig, List<String> configSearchPath, Map<String, Object> cmdLineOptions) {
        List<String> loadedFiles = new ArrayList<String>();
        List<String> loadedFileIndices = new ArrayList<String>();
        
        ConfigParseOptions opt = ConfigParseOptions.defaults();
        opt = opt.setIncluder(new IncluderWrapper(opt.getIncluder(), loadedFiles, loadedFileIndices)).
            setSyntax(ConfigSyntax.CONF).setAllowMissing(false);
        
        Config conf;
        
        if (configSearchPath == null) {
            String envSearchPath = System.getenv("SWIFT_CONF_PATH");
            if (envSearchPath != null) {
                configSearchPath = splitConfigSearchPath(envSearchPath);
            }
        }
        if (configSearchPath == null) {
            conf = loadNormal(cmdLineConfig, opt, loadedFiles, loadedFileIndices);
        }
        else {
            conf = loadFromSearchPath(configSearchPath, cmdLineConfig, opt, 
                loadedFiles, loadedFileIndices);
        }
        
        if (cmdLineOptions != null) {
            Config oconf = ConfigFactory.parseMap(cmdLineOptions, "<Command Line>");
            conf = oconf.withFallback(conf);
            loadedFiles.add("<Command Line>");
            loadedFileIndices.add("C");
        }
        
        conf = conf.resolveWith(getSubstitutions());
        ConfigTree<ValueLocationPair> out = SCHEMA.validate(conf);
        SwiftConfig sc = new SwiftConfig(loadedFiles, loadedFileIndices);
        sc.build(out);
        return sc;
    }

    private static Config loadFromSearchPath(List<String> configSearchPath, String cmdLineConfig,
            ConfigParseOptions opt, List<String> loadedFiles, List<String> loadedFileIndices) {
        Config conf = null;
        
        int index = 1;
        for (String c : configSearchPath) {
            conf = loadOne(c, conf, opt);
            loadedFiles.add(c);
            loadedFileIndices.add(String.valueOf(index++));
        }
        
        if (cmdLineConfig != null) {
            conf = loadOne(cmdLineConfig, conf, opt);
            loadedFiles.add(cmdLineConfig);
            loadedFileIndices.add("R");
        }
        
        return conf;
    }

    private static Config loadNormal(String cmdLineConfig, ConfigParseOptions opt, 
            List<String> loadedFiles, List<String> loadedFileIndices) {
        Config conf;
        
        conf = loadOne(DIST_CONF, null, opt);
        loadedFiles.add(DIST_CONF);
        loadedFileIndices.add("D");
        
        if (SITE_CONF != null) {
            conf = loadOne(SITE_CONF, conf, opt);
            loadedFiles.add(SITE_CONF);
            loadedFileIndices.add("S");
        }
        
        if (USER_CONF != null) {
            conf = loadOne(USER_CONF, conf, opt);
            loadedFiles.add(USER_CONF);
            loadedFileIndices.add("U");
        }
        
        if (cmdLineConfig == null) {
            File runConf = new File("swift.conf");
            if (runConf.exists()) {
                conf = loadOne(runConf.getPath(), conf, opt);
                loadedFiles.add(runConf.getPath());
                loadedFileIndices.add("R");
            }
        }
        
        if (cmdLineConfig != null) {
            conf = loadOne(cmdLineConfig, conf, opt);
            loadedFiles.add(cmdLineConfig);
            loadedFileIndices.add("R");
        }
        
        return conf;
    }

    private static Config loadOne(String fileName, Config conf, ConfigParseOptions opt) {
        File f = new File(fileName);
        Config nconf = ConfigFactory.parseFile(f, opt);
        if (conf == null) {
            return nconf;
        }
        else {
            return nconf.withFallback(conf);
        }
    }

    private static Config getSubstitutions() {
        Map<String, Object> m = new HashMap<String, Object>();
        
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            m.put("env." + e.getKey(), e.getValue());
        }
        
        return ConfigFactory.parseMap(m).withFallback(ConfigFactory.parseProperties(System.getProperties()));
    }
    
    private static SwiftConfig _default;
    
    public synchronized static SwiftConfig getDefault() {
        if (_default == null) {
            throw new IllegalStateException("No default Swift configuration set");
        }
        return _default;
    }
    
    public synchronized static void setDefault(SwiftConfig conf) {
        _default = conf;
    }
        
    private SwiftContactSet definedSites;
    private SwiftContactSet sites;
    private ConfigTree<ValueLocationPair> tree;
    private Map<String, Object> flat;
    private String fileName;
    private List<String> usedFiles;
    private List<String> usedFileIndices;
    
    public SwiftConfig(List<String> usedFiles, List<String> usedFileIndices) {
        definedSites = new SwiftContactSet();
        sites = new SwiftContactSet();
        flat = new HashMap<String, Object>();
        this.usedFiles = usedFiles; 
        this.usedFileIndices = usedFileIndices;
    }
    
    public SwiftContactSet getSites() {
        return sites;
    }
    
    public SwiftContactSet getDefinedSites() {
        return definedSites;
    }
    
    public Collection<String> getDefinedSiteNames() {
        Set<String> s = new TreeSet<String>();
        for (BoundContact bc : definedSites.getContacts()) {
            s.add(bc.getName());
        }
        return s;
    }
    
    public void setProperty(String key, Object value) {
        flat.put(key, value);
    }
    
    public void set(Key key, Object value) {
        flat.put(key.propName, value);
    }
    
    @SuppressWarnings("unchecked")
    private void build(ConfigTree<ValueLocationPair> tree) {
        this.tree = tree;
        List<String> sites = null;
        if (BUILD_CHECK) {
            checkKey("sites");
        }
        ConfigOrigin sitesLoc = null;
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : tree.entrySet()) {
            if (e.getKey().equals("site")) {
                for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> f : e.getValue().entrySet()) {
                    site(definedSites, f.getKey(), f.getValue());
                }
            }
            else if (e.getKey().equals("sites")) {
                sites = (List<String>) getObject(e.getValue());
                sitesLoc = e.getValue().get().loc;
                
            }
            else if (e.getKey().equals("app")) {
                SwiftContact dummy = new SwiftContact();
                apps(dummy, e.getValue());
                for (Application app : dummy.getApplications()) {
                    definedSites.addApplication(app);
                }
            }
        }
        if (sites == null || sites.isEmpty()) {
            throw new RuntimeException("No sites enabled");
        }
        for (String siteName : sites) {
            SwiftContact site = (SwiftContact) definedSites.getContact(siteName);
            if (site == null) {
                throw new RuntimeException(location(sitesLoc) + ": unknown site '" + siteName + "'");
            }
            this.sites.addContact(site);
        }
        this.sites.getApplications().putAll(definedSites.getApplications());
        
        for (String leaf : tree.getLeafPaths()) {
            if ("staging".equals(leaf)) {
                for (BoundContact site : this.sites) {
                    stagingIfNotSet(site, (String) tree.get(leaf).value);
                }
            }
            else {
                flat.put(leaf, tree.get(leaf).value);
            }
        }
    }
    
    public static String location(ConfigOrigin loc) {
        return loc.filename() + ":" + loc.lineNumber();
    }
    

    /**
     * Checks if a key is present in the schema. This is used when building
     * a config from a file to prevent looking for keys that are not allowed
     * by the schema.
     */
    private void checkKey(String key) {
        if (!SCHEMA.isNameValid(key)) {
            throw new IllegalArgumentException("No such property in schema: " + key);
        }
    }

    private void apps(SwiftContact sc, ConfigTree.Node<ValueLocationPair> n) {
        /*
         * app."*" {
         *   ...
         * }
         */
                
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            String k = e.getKey();
            ConfigTree.Node<ValueLocationPair> c = e.getValue();
            
            if (e.getKey().equals("ALL")) {
                sc.addApplication(app("*", e.getValue()));
            }
            else {
                sc.addApplication(app(removeQuotes(e.getKey()), e.getValue()));
            }
        }
        
        Application all = sc.getApplication("*");
        if (all != null) {
            mergeEnvsToApps(sc, all, all.getEnv());
            mergePropsToApps(sc, all.getProperties());
            if (all.getExecutable() == null) {
                sc.removeApplication(all);
            }
        }
    }
        
    private String removeQuotes(String key) {
        if (key.startsWith("\"") && key.endsWith("\"")) {
            return key.substring(1, key.length() - 2);
        }
        else {
            return key;
        }
    }

    private void mergeEnvsToApps(SwiftContact bc, Application srcApp, List<EnvironmentVariable> envs) {
        for (Application app : bc.getApplications()) {
            if (app == srcApp) {
                continue;
            }
            int count = 0;
            for (EnvironmentVariable e : envs) {
                app.insertEnv(count++, e);
            }
        }
    }
    
    private void mergePropsToApps(SwiftContact bc, Map<String, Object> props) {
        for (Application app : bc.getApplications()) {
            for (Map.Entry<String, Object> e : props.entrySet()) {
                if (!app.getProperties().containsKey(e.getKey())) {
                    app.addProperty(e.getKey(), e.getValue());
                }
            }
        }
    }
    
    private Application app(String name, ConfigTree.Node<ValueLocationPair> n) {
        /*
         * app."*" {
         *  executable: "?String"
         *
         *  options: "?Object"
         *  jobType: "?String", queue: "?String", project: "?String"
         *  maxWallTime: "?Time"
         *
         *  env."*": "String"
         * }
         */
        
        Application app = new Application();
        app.setName(name);
        
        if (BUILD_CHECK) {
            checkKey("app.*.executable");
            checkKey("app.*.options");
            checkKey("app.*.env");
            checkKey("app.*.options.jobProject");
            checkKey("app.*.options.jobQueue");
        }
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            String k = e.getKey();
            ConfigTree.Node<ValueLocationPair> c = e.getValue();
            
            if (k.equals("executable")) {
                app.setExecutable(getString(c));
            }
            else if (k.equals("options")) {
                
                for (String key : c.getLeafPaths()) {
                    if (key.equals("jobProject")) {
                        app.addProperty("project", getString(c, key));
                    }
                    else if (key.equals("jobQueue")) {
                        app.addProperty("queue", getString(c, key));
                    }
                    else {
                        app.addProperty(key, getObject(c, key));
                    }
                }
            }
            else if (k.equals("env")) {
                List<KVPair> envs = envs(c);
                for (KVPair env : envs) {
                    app.setEnv(env.key, env.value);
                }
            }
            else {
                app.addProperty(k, getString(c));
            }
        }
                
        return app;
    }


    private List<KVPair> envs(Node<ValueLocationPair> n) {
        List<KVPair> l = new ArrayList<KVPair>();
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            l.add(new KVPair(e.getKey(), getString(e.getValue())));
        }
        return l;
    }

    private void site(SwiftContactSet sites, String name, ConfigTree.Node<ValueLocationPair> n) {
        try {
            SwiftContact sc = new SwiftContact(name);
    
            if (BUILD_CHECK) {
                checkKey("site.*.OS");
                checkKey("site.*.execution");
                checkKey("site.*.filesystem");
                checkKey("site.*.workDirectory");
                checkKey("site.*.scratch");
                checkKey("site.*.app");
                checkKey("site.*.staging");
            }
            
            if (n.hasKey("OS")) {
                SysInfo si = SysInfo.fromString(getString(n, "OS"));
                sc.setProperty("sysinfo", si);
                sc.setProperty("OS", si.getOs());
            }        
            
            for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
                String ctype = e.getKey();
                ConfigTree.Node<ValueLocationPair> c = e.getValue();
                
                if (ctype.equals("execution")) {
                    sc.addService(execution(c));
                }
                else if (ctype.equals("filesystem")) {
                    sc.addService(filesystem(c));
                }
                else if (ctype.equals("workDirectory")) {
                    sc.setProperty("workdir", getString(c));
                }
                else if (ctype.equals("scratch")) {
                    sc.setProperty("scratch", getString(c));
                }
                else if (ctype.equals("app")) {
                    apps(sc, c);
                }
                else if (ctype.equals("staging")) {
                    staging(sc, c);
                }
                else if (ctype.equals("OS")) {
                    // handled above
                }
                else {
                    sc.setProperty(ctype, getObject(c));
                }
            }
            sites.addContact(sc);
        }
        catch (Exception e) {
            throw new RuntimeException("Invalid site entry '" + name + "': ", e);
        }
    }
    
    private void stagingIfNotSet(BoundContact sc, String staging) {
        if (sc.getProperty("staging") != null) {
            return;
        }
        staging(sc, staging);
    }
    
    private void staging(BoundContact sc, String staging) {
        if (staging.equals("swift") || staging.equals("wrapper")) {
            sc.setProperty("staging", staging);
        }
        else if (staging.equals("local")) {
            sc.setProperty("staging", "provider");
            sc.setProperty("stagingMethod", "file");
        }
        else if (staging.equals("direct")) {
            sc.setProperty("staging", "provider");
            sc.setProperty("stagingMethod", "direct");
        }
        else if (staging.equals("service-local")) {
            sc.setProperty("staging", "provider");
            sc.setProperty("stagingMethod", "cs");
        }
        else if (staging.equals("shared-fs")) {
            sc.setProperty("staging", "provider");
            sc.setProperty("stagingMethod", "sfs");
        }
    }

    private void staging(SwiftContact sc, Node<ValueLocationPair> n) {
        String staging = getString(n);
        if (BUILD_CHECK) {
            checkValue("site.*.staging", 
                "swift", "wrapper", "local", "service-local", "shared-fs", "direct");
        }
        staging(sc, staging);
    }

    private boolean isCoaster(SwiftContact sc) {
        for (Map.Entry<BoundContact.TypeProviderPair, Service> e : sc.getServices().entrySet()) {
            if (e.getKey().provider != null && e.getKey().type == Service.EXECUTION) {
                return e.getKey().provider.startsWith("coaster");
            }
        }
        return false;
    }

    private void checkValue(String key, String... values) {
        SwiftConfigSchema.Info i = SCHEMA.getInfo(key);
        if (i == null) {
            throw new IllegalArgumentException("No type information found for: " + key);
        }
        for (String v : values) {
            i.type.check(key, v, i.loc);
        }
    }

    private Service filesystem(Node<ValueLocationPair> c) throws InvalidProviderException, ProviderMethodException {        
        Service s = new ServiceImpl();
        s.setType(Service.FILE_OPERATION);
        fileService(c, s);
        return s;
    }
    
    private void fileService(Node<ValueLocationPair> n, Service s) throws InvalidProviderException, ProviderMethodException {
        String provider = null;
        String url = null;
        if (BUILD_CHECK) {
            checkKey("site.*.filesystem.type");
            checkKey("site.*.filesystem.URL");
            checkKey("site.*.filesystem.options");
        }
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            String k = e.getKey();
            ConfigTree.Node<ValueLocationPair> c = e.getValue();
            
            if (k.equals("type")) {
                provider = getString(c);
            }
            else if (k.equals("URL")) {
                url = getString(c);
            }
            else if (k.equals("options")) {
                for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> f : e.getValue().entrySet()) {
                    s.setAttribute(f.getKey(), getObject(f.getValue()));
                }
            }
        }
        
        s.setProvider(provider);
        if (url != null) {
            ServiceContact contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
    }


    private Service execution(ConfigTree.Node<ValueLocationPair> n) throws InvalidProviderException, ProviderMethodException {
        ExecutionService s = new ExecutionServiceImpl();
        execService(n, s);
        return s;
    }

    private void execService(Node<ValueLocationPair> n, Service s) throws InvalidProviderException, ProviderMethodException {
        String provider = null;
        String url = null;
        if (BUILD_CHECK) {
            checkKey("site.*.execution.type");
            checkKey("site.*.execution.URL");
            checkKey("site.*.execution.jobManager");
            checkKey("site.*.execution.options");
        }
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            String k = e.getKey();
            ConfigTree.Node<ValueLocationPair> c = e.getValue();
            
            if (k.equals("type")) {
                provider = getString(c);
            }
            else if (k.equals("URL")) {
                url = getString(c);
            }
            else if (k.equals("jobManager")) {
                ((ExecutionService) s).setJobManager(getString(c));
            }
            else if (k.equals("options")) {
                execOptions((ExecutionService) s, c);
            }
            
        }
        
        s.setProvider(provider);
        s.setIdentity(new IdentityImpl());
        if (url != null) {
            ServiceContact contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
    }

    private void execOptions(ExecutionService s, Node<ValueLocationPair> n) {
        if (BUILD_CHECK) {
            checkKey("site.*.execution.options.jobProject");
            checkKey("site.*.execution.options.maxJobs");
            checkKey("site.*.execution.options.maxJobTime");
            checkKey("site.*.execution.options.maxNodesPerJob");
            checkKey("site.*.execution.options.jobQueue");
            checkKey("site.*.execution.options.jobOptions");
        }
        
        for (Map.Entry<String, ConfigTree.Node<ValueLocationPair>> e : n.entrySet()) {
            String k = e.getKey();
            ConfigTree.Node<ValueLocationPair> c = e.getValue();
            
            
            if (k.equals("jobProject")) {
                s.setAttribute("project", getObject(c));
            }
            else if (k.equals("maxJobs")) {
                s.setAttribute("slots", getObject(c));
            }
            else if (k.equals("maxJobTime")) {
                s.setAttribute("maxTime", getObject(c));
            }
            else if (k.equals("maxNodesPerJob")) {
                s.setAttribute("maxNodes", getObject(c));
            }
            else if (k.equals("jobQueue")) {
                s.setAttribute("queue", getObject(c));
            }
            else if (k.equals("tasksPerNode")) {
                s.setAttribute("jobsPerNode", getObject(c));
            }
            else if (k.equals("jobOptions")) {
                for (String key : c.getLeafPaths()) {
                    s.setAttribute(key, getObject(c, key));
                }
            }
            else {
                s.setAttribute(k, getObject(c));
            }
        }
    }

    private String getString(Node<ValueLocationPair> c) {
        return (String) c.get().value;
    }

    private Object getObject(ConfigTree.Node<ValueLocationPair> c) {
        return c.get().value;
    }
    
    private String getString(ConfigTree.Node<ValueLocationPair> c, String key) {
        return (String) c.get(key).value;
    }
    
    private Object getObject(ConfigTree.Node<ValueLocationPair> c, String key) {
        return c.get(key).value;
    }
    
    private void checkType(String name, ConfigValue value, ConfigValueType type) {
        if (!type.equals(value.valueType())) {
            throw new SwiftConfigException(value.origin(), 
                "'" + name + "': wrong type (" + value.valueType() + "). Must be a " + type);
        }
    }
    
    public Object clone() {
        return this;
    }
    
    public String toString() {
        return toString(true, true);
    }
    
    public String toString(boolean files, boolean values) {
        StringBuilder sb = new StringBuilder();
        if (files) {
            for (int i = 0; i < usedFiles.size(); i++) {
                sb.append("[" + usedFileIndices.get(i) + "] " + usedFiles.get(i) + "\n");
            }
        }
        if (values) {
            sb.append(tree.toString(true, new LocationFormatter(usedFiles, usedFileIndices, tree)));
        }
        return sb.toString();
    }
    
    private static class LocationFormatter extends ConfigTree.DefaultValueFormatter {
        private List<String> files, fileIndices;
        private ConfigTree<ValueLocationPair> tree;
        
        public LocationFormatter(List<String> files, List<String> fileIndices, ConfigTree<ValueLocationPair> tree) {
            this.files = files;
            this.fileIndices = fileIndices;
            this.tree = tree;
        }

        @Override
        public void format(String key, String full, Object value, int indentationLevel, StringBuilder sb) {
            ValueLocationPair p = tree.get(full);
            if (p.loc == null) {
                // properties with default values from the schema, so don't print those
                return;
            }
            int start = sb.length();
            for (int i = 0; i < indentationLevel; i++) {
                sb.append('\t');
            }
            sb.append(key);
            sb.append(": ");
            
            if (p.value instanceof String) {
                sb.append("\"");
            }
            sb.append(p.value);
            if (p.value instanceof String) {
                sb.append("\"");
            }
            
            int width = sb.length() - start;
            
            for (int i = 0; i < 60 - width; i++) {
                sb.append(' ');
            }
            
            sb.append("# [");
            int index;
            if (p.loc.filename() == null) {
                // command line
                index = files.size() - 1;
            }
            else {
                index = files.indexOf(p.loc.filename());
            }
            if (index == -1) {
                sb.append('?');
            }
            else {
                sb.append(fileIndices.get(index));
            }
            sb.append("]");
            if (p.loc.filename() != null) {
                sb.append(" line ");
                sb.append(p.loc.lineNumber());
            }
            sb.append('\n');
        }
    }
    
    private void check(String name) {
        if (CHECK_DYNAMIC_NAMES) {
            if (!SCHEMA.isNameValid(name)) {
                throw new IllegalArgumentException("Unknown property name: '" + name + "'");
            }
        }
    }
    
    private Object get(Key k) {
        return flat.get(k.propName);
    }

    public Object getProperty(String name) {
        check(name);
        return flat.get(name);
    }

    public String getStringProperty(String name) {
        check(name);
        return (String) flat.get(name);
    }

    public boolean isFileGCEnabled() {
        return (Boolean) get(Key.FILE_GC_ENABLED);
    }

    public boolean isTickerEnabled() {
        return (Boolean) get(Key.TICKER_ENABLED);
    }

    public String getTickerDateFormat() {
        return (String) get(Key.TICKER_DATE_FORMAT);
    }

    public String getTickerPrefix() {
        return (String) get(Key.TICKER_PREFIX);
    }

    public boolean isTracingEnabled() {
        return (Boolean) get(Key.TRACING_ENABLED);
    }

    public Object getProperty(String name, Object defVal) {
        check(name);
        Object v = flat.get(name);
        if (v == null) {
            return defVal;
        }
        else {
            return v;
        }
    }
    
    public int getForeachMaxThreads() {
        return (Integer) get(Key.FOREACH_MAX_THREADS);
    }

    public String getCachingAlgorithm() {
        return (String) get(Key.CACHING_ALGORITHM);
    }

    public boolean isReplicationEnabled() {
        return (Boolean) get(Key.REPLICATION_ENABLED);
    }

    public String getWrapperStagingLocalServer() {
        return (String) get(Key.WRAPPER_STAGING_LOCAL_SERVER);
    }
    
    public boolean isProvenanceEnabled() {
        return (Boolean) get(Key.PROVENANCE_LOG);
    }

    public int getReplicationMinQueueTime() {
        return (Integer) get(Key.REPLICATION_MIN_QUEUE_TIME);
    }

    public int getReplicationLimit() {
        return (Integer) get(Key.REPLICATION_LIMIT);
    }

    public String getWrapperInvocationMode() {
        return (String) get(Key.WRAPPER_INVOCATION_MODE);
    }

    public boolean isMappingCheckerEnabled() {
        return (Boolean) get(Key.DM_CHECKER);
    }

    public String getCDMBroadcastMode() {
        return (String) get(Key.CDM_BROADCAST_MODE);
    }

    public String getCDMFile() {
        return (String) get(Key.CMD_FILE);
    }
}
