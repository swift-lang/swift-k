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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;

public class AbstractionProperties extends java.util.Properties {

    private static Logger logger = Logger
            .getLogger(AbstractionProperties.class);
    public static final String PROVIDER_PROPERTY_FILE = "cog-provider.properties";
    public static final String CLASS_LOADER_NAME = "classloader.name";
    public static final String CLASS_LOADER_PROPERTIES = "classloader.properties";
    public static final String CLASS_LOADER_BOOTCLASS = "classloader.boot";
    public static final String CLASS_LOADER_USESYSTEM = "classloader.usesystem";
    public static final String SANDBOX = "sandbox";
    public static final String SANDBOX_BOOTCLASS = "sandbox.boot";
    public static final String ALIAS = "alias";

    public static final String TYPE_FILE_RESOURCE = "fileResource";
    public static final String TYPE_EXECUTION_TASK_HANDLER = "executionTaskHandler";
    public static final String TYPE_FILE_TRANSFER_TASK_HANDLER = "fileTransferTaskHandler";
    public static final String TYPE_FILE_OPERATION_TASK_HANDLER = "fileOperationTaskHandler";
    public static final String TYPE_SECURITY_CONTEXT = "securityContext";

    private static Map<String, AbstractionProperties> providerProperties;

    private static Map<String, String> aliases;

    /**
     * Returns a list with the names of all the providers known in this JVM
     * instance.
     */
    public static List<String> getProviders() {
        loadProviderProperties();
        return new LinkedList<String>(providerProperties.keySet());
    }

    /**
     * Returns a list with the names of all providers known to have the
     * specified property. For example, you can use
     * <code>getProviders({@link TYPE_EXECUTION_TASK_HANDLER})</code> to
     * retrieve a list of a all providers that have an excution task handler.
     */
    public static List<String> getProviders(String type) {
        loadProviderProperties();
        type = type.toLowerCase();
        List<String> l = new LinkedList<String>();
        for (Map.Entry<String, AbstractionProperties> e : providerProperties.entrySet()) {
            String name = e.getKey();
            AbstractionProperties props = e.getValue();
            if (props.containsKey(type)) {
                l.add(name);
            }
        }
        return l;
    }
    
    public static List<String> getAliases(String provider) {
        List<String> l = new ArrayList<String>();
        for (Map.Entry<String, String> e : aliases.entrySet()) {
            if (provider.equals(e.getValue())) {
                l.add(e.getKey());
            }
        }
        return l;
    }

    public static String getAliasesAsString() {
        if (aliases == null || aliases.size() == 0) {
            return "none";
        }
        else {
            Map<String, List<String>> m = new HashMap<String, List<String>>();
            for (Map.Entry<String, String> e : aliases.entrySet()) {
                if (!m.containsKey(e.getValue())) {
                    m.put(e.getValue(), new LinkedList<String>());
                }
                m.get(e.getValue()).add(e.getKey());
            }
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, List<String>> e : m.entrySet()) {
                sb.append(e.getKey());
                sb.append(" <-> ");
                Iterator<String> j = e.getValue().iterator();
                while (j.hasNext()) {
                    sb.append(j.next());
                    if (j.hasNext()) {
                        sb.append(", ");
                    }
                    else {
                        sb.append("; ");
                    }
                }
            }
            return sb.toString();
        }
    }

    public static AbstractionProperties getProperties(String provider)
            throws InvalidProviderException {
        loadProviderProperties();
        provider = provider.toLowerCase();
        if (aliases.containsKey(provider)) {
            provider = aliases.get(provider);
        }
        if (providerProperties.containsKey(provider)) {
            return providerProperties.get(provider);
        }
        else {
            logger.info("No properties for provider " + provider
                    + ". Using empty properties");
            if (logger.isDebugEnabled()) {
                logJars();
            }
            throw new InvalidProviderException("No '" + provider
                    + "' provider or alias found. Available providers: "
                    + getProviders() + ". Aliases: " + getAliasesAsString());
        }
    }

    private static void logJars() {
        URL[] urls = ((URLClassLoader) AbstractionProperties.class
                .getClassLoader()).getURLs();
        logger
                .debug("================ Jars found on classpath =================");
        for (int i = 0; i < urls.length; i++) {
            logger.debug(urls[i]);
        }
        logger
                .debug("==========================================================");
    }

    protected synchronized static void loadProviderProperties() {
        if (providerProperties != null) {
            return;
        }
        try {
            providerProperties = new HashMap<String, AbstractionProperties>();
            aliases = new HashMap<String, String>();
            Enumeration<URL> e = AbstractionFactory.class.getClassLoader()
                    .getResources(PROVIDER_PROPERTY_FILE);
            while (e.hasMoreElements()) {
                try {
                    loadProviderProperties(e.nextElement().openStream());
                }
                catch (Exception ee) {
                    logger.warn("Error reading from provider properties", ee);
                }
            }
        }
        catch (Exception e) {
            logger
                    .warn("No "
                            + PROVIDER_PROPERTY_FILE
                            + " resource found. You should have at least one provider.");
        }
    }

    private static void loadProviderProperties(InputStream is) {
        Properties props = new Properties();
        try {
            props.load(is);
            AbstractionProperties map = null;
            AbstractionProperties common = new AbstractionProperties();
            String classLoader = null;
            String classLoaderProps = null;
            String classLoaderBoot = null;
            boolean nameFound = false;
            for (Property prop : props) {
                if (prop.name.equalsIgnoreCase("provider")) {
                    map = new AbstractionProperties();
                    map.putAll(common);
                    providerProperties
                            .put(prop.value.trim().toLowerCase(), map);
                    nameFound = true;
                }
                else if (prop.name.equalsIgnoreCase("alias")) {
                    String[] alias = prop.value.split(":");
                    if (alias.length != 2) {
                        logger.warn("Invalid alias line: " + prop.name + "="
                                + prop.value);
                    }
                    else {
                        aliases.put(alias[0], alias[1]);
                    }
                }
                else {
                    if (map == null) {
                        common.put(prop.name.trim().toLowerCase(), prop.value
                                .trim());
                    }
                    else {
                        map.put(prop.name.trim().toLowerCase(), prop.value
                                .trim());
                    }
                }
            }
            if (!nameFound) {
                logger
                        .warn("Provider name missing from provider properties file");
            }
        }
        catch (Exception e) {
            logger.warn("Could not load properties", e);
        }
    }

    public AbstractionProperties() {
    }

    public AbstractionProperties(String file) throws IOException {
        load(file);
    }

    public void load(String file) throws IOException {
        FileInputStream in = null;
        try {
            File f = new File(file);
            if (f.exists()) {
                in = new FileInputStream(f);
                load(in);
            }
        }
        catch (IOException e) {
            logger.warn("Could not load properties from file: " + file);
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ee) {
                }
            }
            throw e;
        }
    }

    public String getProperty(String key) {
        return (String) get(key.toLowerCase());
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key)).booleanValue();
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        try {
            return getBooleanProperty(name);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public void put(String key, String value) {
        if (value == null) {
            return;
        }
        super.put(key.toLowerCase(), value);
    }

    public void load(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);
        for (Property prop : props) {
            put(prop.name, prop.value);
        }
    }

    public static class Properties extends LinkedList<Property> {

        public void load(InputStream is) throws IOException {
            BufferedReader isr = new BufferedReader(new InputStreamReader(is));
            String line = isr.readLine();
            while (line != null) {
                if (!line.trim().equals("") && !line.startsWith("#")) {
                    add(new Property(line));
                }
                line = isr.readLine();
            }
        }
    }

    public static class Property {

        public String name;
        public String value;

        public Property(String line) {
            String[] prop = splitProperty(line);
            name = prop[0];
            value = prop[1];
        }

        public static String[] splitProperty(String property) {
            StringTokenizer st = new StringTokenizer(property, "=");
            String name = null;
            String value = null;
            if (st.hasMoreTokens()) {
                name = st.nextToken();
            }
            if (st.hasMoreTokens()) {
                value = st.nextToken();
                while (st.hasMoreTokens()) {
                    value = value + "=" + st.nextToken();
                }
            }
            if (name == null) {
                return new String[] { "", "" };
            }
            if (value == null) {
                value = "";
            }
            value = replaceProperties(value);
            return new String[] { name.trim().toLowerCase(), value.trim() };
        }

        protected static String replaceProperties(String value) {
            if (value == null) {
                return null;
            }
            int bi = -1;
            do {
                bi = value.indexOf("${", bi + 1);
                if (bi != -1) {
                    int ei = value.indexOf("}", bi);
                    if (ei > bi) {
                        String name = value.substring(bi + 2, ei);
                        String prop = System.getProperty(name, "${" + name
                                + "}");
                        value = value.substring(0, bi) + prop
                                + value.substring(ei + 1);
                        continue;
                    }
                }
            } while (bi >= 0);
            return value;
        }
    }
}