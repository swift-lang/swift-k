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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.util.SwiftConfig.ValueLocationPair;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;

public class SwiftConfigSchema {
    private static final String STAR = "*";
    
    public static class Info {
        public ConfigPropertyType<?> type;
        public String typeSpec;
        public Object value;
        public boolean optional;
        public String doc;
        public ConfigOrigin loc;
        
        public String toString() {
            return String.valueOf(type);
        }
    }
    
    private Config schema;
    private ConfigTree<Info> info;
    private Map<String, Info> descriptions;
    private Set<String> validNames;
    
    public SwiftConfigSchema() {
        validNames = new HashSet<String>();
        schema = ConfigFactory.parseResources("swift.conf.schema");
        schema = schema.resolve();
        if (schema.isEmpty()) {
            throw new RuntimeException("Could not find swift.conf.schema");
        }
        info = new ConfigTree<Info>();
        for (Map.Entry<String, ConfigValue> e : schema.entrySet()) {
            String k = e.getKey();
            String nk = k.replace("\"*\"", "*");
            String type = null;
            Object defaultValue = null;
            String doc = null;
            ConfigOrigin loc = null;
            if (k.endsWith(".\"_type\"")) {
                type = schema.getString(k);
                nk = nk.substring(0, nk.lastIndexOf('.'));
                loc = e.getValue().origin();
            }
            else if (k.indexOf(".\"_") == -1){
                type = schema.getString(k);
                loc = e.getValue().origin();
            }
            else if (k.endsWith(".\"_default\"")){
                defaultValue = e.getValue().unwrapped();
                nk = nk.substring(0, nk.lastIndexOf('.'));
            }
            else if (k.endsWith(".\"_doc\"")){
                doc = stripDoc((String) e.getValue().unwrapped());
                nk = nk.substring(0, nk.lastIndexOf('.'));
            }
            else if (k.indexOf(".\"_") != -1) {
                continue;
            }
            Info i = info.get(nk);
            if (i == null) {
                i = new Info();
                info.put(nk, i);
                setValid(nk);
            }
            if (type != null) {
                if (type.startsWith("?")) {
                    i.optional = true;
                    type = type.substring(1);
                }
                i.type = getTypeInstance(type, e.getValue());
                i.typeSpec = type;
            }
            if (defaultValue != null) {
                i.value = defaultValue;
            }
            if (doc != null) {
                i.doc = doc;
            }
            if (loc != null) {
                i.loc = loc;
            }
        }
    }

    private void setValid(String k) {
        validNames.add(k);
        if (!k.isEmpty()) {
            setValid(parent(k));
        }
    }

    private String stripDoc(String doc) {
        return doc.replaceAll("[\\t\\n]+", " ");
    }

    private ConfigPropertyType<?> getTypeInstance(String type, ConfigValue value) {
        if (type.equals("String")) {
            return ConfigPropertyType.STRING;
        }
        else if (type.equals("URI")) {
            return ConfigPropertyType.URI;
        }
        else if (type.equals("Boolean")) {
            return ConfigPropertyType.BOOLEAN;
        }
        else if (type.equals("Int")) {
            return ConfigPropertyType.INT;
        }
        else if (type.equals("Float")) {
            return ConfigPropertyType.FLOAT;
        }
        else if (type.equals("StrictlyPositiveInt")) {
            return ConfigPropertyType.STRICTLY_POSITIVE_INT;
        }
        else if (type.equals("Throttle")) {
            return ConfigPropertyType.THROTTLE;
        }
        else if (type.equals("PortRange")) {
            return ConfigPropertyType.PORT_RANGE;
        }
        else if (type.equals("PositiveInt")) {
            return ConfigPropertyType.POSITIVE_INT;
        }
        else if (type.equals("PositiveFloat")) {
            return ConfigPropertyType.POSITIVE_FLOAT;
        }
        else if (type.equals("Time")) {
            return ConfigPropertyType.TIME;
        }
        else if (type.equals("Seconds")) {
            return ConfigPropertyType.STRICTLY_POSITIVE_INT;
        }
        else if (type.equals("OS")) {
            return ConfigPropertyType.OS;
        }
        else if (type.equals("StringList")) {
            return ConfigPropertyType.STRING_LIST;
        }
        else if (type.equals("Object")) {
            return ConfigPropertyType.OBJECT;
        }
        else if (type.startsWith("Choice[")) {
            String values = type.substring("Choice[".length(), type.length() - 1);
            return new ConfigPropertyType.Choices(values.split(",\\s*"));
        }
        else if (type.startsWith("Interval[")) {
            String values = type.substring("Interval[".length(), type.length() - 1);
            String[] s = values.split(",\\s*", 2);
            return new ConfigPropertyType.Interval(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        }
        else {
            throw new IllegalArgumentException(loc(value.origin()) + ": unknown property type: '" + type + "'");
        }
    }

    private String loc(ConfigOrigin o) {
        return o.filename() + ":" + o.lineNumber();
    }

    public ConfigTree<ValueLocationPair> validate(Config conf) {
        ConfigTree<ValueLocationPair> validated = new ConfigTree<ValueLocationPair>();
        
        // build a tree of the actual config so we can easily check them for missing properties
        ConfigTree<Boolean> confTree = new ConfigTree<Boolean>();
        
        // check things in config against schema
        for (Map.Entry<String, ConfigValue> e : conf.entrySet()) {
            String k = e.getKey();
            confTree.put(k, Boolean.TRUE);
            // check if the properties are defined in the schema
            Info i = info.get(k, STAR);
            if (i == null) {
                i = info.get(k, STAR);
                throw new SwiftConfigException(e.getValue().origin(), "unexpected property '" + k + "'");
            }
            // now check values
            if (i.type == null) {
                throw new IllegalStateException("Missing type for key " + k);
            }
            Object value = checkValue(k, e.getValue(), i.type);
            validated.put(k, new ValueLocationPair(value, e.getValue().origin()));
        }
        
        // check for missing things
        for (String key : info.getLeafPaths()) {
            Info i = info.get(key);
            String notFound = findMissing(key, confTree, i, validated);
            if (notFound != null) {
                findMissing(key, confTree, i, validated);
                ConfigOrigin loc = info.get(key).loc;
                throw new SwiftConfigException(conf.origin(), "missing mandatory property '" + notFound + "'");
            }
        }
        return validated;
    }

    private String findMissing(String key, ConfigTree<Boolean> confTree, Info i, ConfigTree<ValueLocationPair> validated) {
        List<String> found = confTree.expandWildcards(key, STAR);

        for (String f : found) {
            if (!confTree.hasKey(f)) {
                if (i.optional) {
                    if (i.value != null) {
                        validated.put(f, new ValueLocationPair(i.value, null));
                    }
                }
                else if (!parentsAreOptional(key, confTree)) {
                    return f;
                }
            }
        }
        return null;
    }

    private boolean parentsAreOptional(String k, ConfigTree<Boolean> confTree) {
        while (!k.isEmpty()) {
            k = parent(k);
            if (k.endsWith(".*")) {
                // if you got to this point and expansion has been performed,
                // the parents are not optional any more
                return false;
            }
            Info i = info.get(k);
            if (i != null) {
                if (i.optional) {
                    if (confTree.hasKey(k)) {
                        // continue checking parents
                    }
                    else {
                        return true;
                    }
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    private String parent(String k) {
        int ix = k.lastIndexOf('.');
        if (ix == -1) {
            return "";
        }
        else {
            return k.substring(0, ix);
        }
    }

    private void setValue(Map<String, Object> validated, Config conf, String k) {
        if (!conf.hasPath(k)) {
            String defKey = k + ".\"_default\"";
            if (schema.hasPath(defKey)) {
                validated.put(k, schema.getValue(defKey).unwrapped());
            }
        }
        else {
            validated.put(k, conf.getValue(k).unwrapped());
        }
    }

    private Object checkValue(String k, ConfigValue value, ConfigPropertyType<?> t) {
        Object v = value.unwrapped();
        switch (value.valueType()) {
            case STRING:
                // allow auto-conversion from string
                return t.check(k, value.unwrapped(), value.origin());
            case NUMBER:
                if (t.getBaseType() != ConfigPropertyType.INT && t.getBaseType() != ConfigPropertyType.FLOAT) {
                    throw invalidValue(value, k, v, t.getBaseType());
                }
                if (t.getBaseType() == ConfigPropertyType.INT) {
                    Number n = (Number) value.unwrapped();
                    if (n.intValue() != n.doubleValue()) {
                        throw invalidValue(value, k, v, t.getBaseType());
                    }
                }
                return t.check(k, v, null);
            case BOOLEAN:
                if (t.getBaseType() != ConfigPropertyType.BOOLEAN) {
                    throw invalidValue(value, k, v, t.getBaseType());
                }
                return value.unwrapped();
            default:
                return t.check(k, v, value.origin());
        }
    }

    private SwiftConfigException invalidValue(ConfigValue value, String k, Object v, ConfigPropertyType<?> t) {
        switch (t.toString().charAt(0)) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return new SwiftConfigException(value.origin(), "invalid value '" + v + "' for property '" + k + "'. Expected an " + t);
            default:
                return new SwiftConfigException(value.origin(), "invalid value '" + v + "' for property '" + k + "'. Expected a " + t);
        }
    }
    
    public synchronized Map<String, Info> getPropertyDescriptions() {
        if (descriptions == null) {
            descriptions = new HashMap<String, Info>();
            for (String s : info.getLeafPaths()) {
                Info i = info.get(s);
                if (i != null && i.doc != null) {
                    descriptions.put(s, i);
                }
            }
        }
        return descriptions;
    }

    public boolean propertyExists(String name) {
        return info.get(name) != null;
    }

    public boolean isNameValid(String name) {
        return validNames.contains(name);
    }

    public Collection<String> listProperties() {
        return validNames;
    }

    public Info getInfo(String key) {
        return info.get(key);
    }
    
    public ConfigTree<Info> getInfoTree() {
        return info;
    }
}
