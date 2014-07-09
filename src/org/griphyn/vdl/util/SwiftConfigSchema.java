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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;

public class SwiftConfigSchema {
    private static final String STAR = "\"*\"";
    
    public static class Info {
        public ConfigPropertyType<?> type;
        public Object value;
        public boolean optional;
        public String doc;
        public ConfigOrigin loc;
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
            String type = null;
            Object defaultValue = null;
            String doc = null;
            ConfigOrigin loc = null;
            if (k.endsWith(".\"_type\"")) {
                type = schema.getString(k);
                k = k.substring(0, k.lastIndexOf('.'));
                loc = e.getValue().origin();
            }
            else if (k.indexOf(".\"_") == -1){
                type = schema.getString(k);
                loc = e.getValue().origin();
            }
            else if (k.endsWith(".\"_default\"")){
                defaultValue = e.getValue().unwrapped();
                k = k.substring(0, k.lastIndexOf('.'));
            }
            else if (k.endsWith(".\"_doc\"")){
                doc = stripDoc((String) e.getValue().unwrapped());
                k = k.substring(0, k.lastIndexOf('.'));
            }
            else if (k.indexOf(".\"_") != -1) {
                continue;
            }
            Info i = info.get(k);
            if (i == null) {
                i = new Info();
                info.put(k, i);
                validNames.add(k);
            }
            if (type != null) {
                if (type.startsWith("?")) {
                    i.optional = true;
                    type = type.substring(1);
                }
                i.type = getTypeInstance(type, e.getValue());
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

    private String stripDoc(String doc) {
        return doc.replaceAll("[\\t\\n]+", "");
    }

    private ConfigPropertyType<?> getTypeInstance(String type, ConfigValue value) {
        if (type.equals("String")) {
            return ConfigPropertyType.STRING;
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

    public ConfigTree<Object> validate(Config conf) {
        ConfigTree<Object> validated = new ConfigTree<Object>();
        
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
            validated.put(k, value);
        }
        
        // check for missing things
        for (String key : info.getLeafPaths()) {
            Info i = info.get(key);
            String notFound = findMissing(key, confTree, i, validated);
            if (notFound != null) {
                findMissing(key, confTree, i, validated);
                ConfigOrigin loc = info.get(key).loc;
                throw new SwiftConfigException(conf.origin(), "missing property '" + notFound + 
                    "' defined in " + loc.filename() + ":" + loc.lineNumber());
            }
        }
        return validated;
    }

    private String findMissing(String key, ConfigTree<Boolean> confTree, Info i, ConfigTree<Object> validated) {
        List<String> found = confTree.expandWildcards(key, STAR);
        for (String f : found) {
            if (!confTree.hasKey(f)) {
                if (i.optional) {
                    if (i.value != null) {
                        validated.put(f, i.value);
                    }
                }
                else {
                    return f;
                }
            }
        }
        return null;
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
                if (t.getBaseType() != ConfigPropertyType.STRING && t.getBaseType() != ConfigPropertyType.OBJECT) {
                    throw invalidValue(value, k, v, t.getBaseType());
                }
                return t.check(k, value.unwrapped(), null);
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
                return t.check(k, v, null);
        }
    }

    private SwiftConfigException invalidValue(ConfigValue value, String k, Object v, ConfigPropertyType<?> t) {
        return new SwiftConfigException(value.origin(), "invalid value '" + v + "' for property '" + k + "'. Expected a " + t);
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
}
