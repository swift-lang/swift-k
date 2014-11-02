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
 * Created on Sep 6, 2013
 */
package org.griphyn.vdl.util;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.swift.catalog.types.Arch;
import org.globus.swift.catalog.types.Os;

import com.typesafe.config.ConfigOrigin;

public abstract class ConfigPropertyType<T> {
    public static final ConfigPropertyType<Object> BOOLEAN = new CPTBoolean();
    public static final ConfigPropertyType<String> STRING = new CPTString();
    public static final ConfigPropertyType<String> URI = new CPTURI();
    public static final ConfigPropertyType<Object> INT = new Int();
    public static final ConfigPropertyType<Object> THROTTLE = new Throttle();
    public static final ConfigPropertyType<String> PORT_RANGE = new PortRange();
    public static final ConfigPropertyType<Object> STRICTLY_POSITIVE_INT = new SPInt();
    public static final ConfigPropertyType<Object> POSITIVE_INT = new PInt();
    public static final ConfigPropertyType<Object> POSITIVE_FLOAT = new PFloat();
    public static final ConfigPropertyType<Object> FLOAT = new CPTFloat();
    public static final ConfigPropertyType<String> FILE = new CPTFile();
    public static final ConfigPropertyType<String> TIME = new CPTTime();
    public static final ConfigPropertyType<Object> OBJECT = new CPTObject();
    public static final ConfigPropertyType<Object> STRING_LIST = new StringList();
    public static final ConfigPropertyType<String> OS = new CPTOS();
    
    public static ConfigPropertyType<String> choices(String... values) {
        return new Choices(values);
    }
    
    @SuppressWarnings("unchecked")
    public Object check(String propName, Object value, ConfigOrigin loc) {
        return checkValue(propName, (T) value, loc);
    }
    
    public abstract Object checkValue(String propName, T value, ConfigOrigin loc);
    
    public abstract ConfigPropertyType<?> getBaseType();
    
    protected RuntimeException cannotConvert(ConfigOrigin loc, String propName, Object value, String toWhat) {
        return new IllegalArgumentException(location(loc) + ":\n\tCannot convert value '" + value + "' for property '" + 
                propName + "' to " + toWhat);
    }
        
    private static String pp(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = c.iterator();
        while (i.hasNext()) {
            sb.append('\'');
            sb.append(i.next());
            sb.append('\'');
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    public static class Choices extends ConfigPropertyType<String> {
        protected SortedSet<String> choices;
        
        public Choices(String... values) {
            choices = new TreeSet<String>();
            for (String value : values) {
                choices.add(value);
            }
        }

        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            if (!choices.contains(value)) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Valid values are: " + pp(choices));
            }
            return value;
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }

        @Override
        public String toString() {
            return "one of " + choices;
        }
    }
    
    private static class CPTOS extends Choices {
        public CPTOS() {
            super();
            int ix = 0;
            for (Arch a : Arch.values()) {
                for (Os o : Os.values()) {
                    choices.add(a + "::" + o);
                }
            }
        }
    }
    
    private static class CPTString extends ConfigPropertyType<String> {
        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            // all values accepted
            return value;
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }
        
        @Override
        public String toString() {
            return "string";
        }
    }
    
    private static class CPTURI extends ConfigPropertyType<String> {
        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            try {
                URI u = new URI(value);
                return value;
            }
            catch (Exception e) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'");
            }
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }
        
        @Override
        public String toString() {
            return "URI";
        }
    }
    
    private static class Int extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                }
                catch (NumberFormatException e) {
                    throw cannotConvert(loc, propName, value, "integer");
                }
            }
            else if (value instanceof Integer) {
                return value;
            }
            else {
                throw cannotConvert(loc, propName, value, "integer");
            }
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return INT;
        }
        
        @Override
        public String toString() {
            return "integer";
        }
    }
    
    private static class CPTBoolean extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
            else if (value instanceof Boolean) {
                return value;
            }
            else {
                throw cannotConvert(loc, propName, value, "boolean");
            }
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return BOOLEAN;
        }
        
        @Override
        public String toString() {
            return "boolean";
        }
    }
    
    private static class SPInt extends Int {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            Integer ivalue = (Integer) super.checkValue(propName, value, loc);
            if (ivalue <= 0) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                propName + "'. Must be a " + toString());
            }
            return ivalue;
        }
        
        @Override
        public String toString() {
            return "strictly positive integer";
        }
    }
    
    private static class PInt extends Int {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            Integer ivalue = (Integer) super.checkValue(propName, value, loc);
            if (ivalue < 0) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                propName + "'. Must be a " + toString());
            }
            return ivalue;
        }
        
        @Override
        public String toString() {
            return "positive integer";
        }
    }

    
    private static class Throttle extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            if ("off".equals(value)) {
                return Integer.MAX_VALUE;
            }
            else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                }
                catch (NumberFormatException e) {
                    throw cannotConvert(loc, propName, value, "integer");
                }
            }
            else if (value instanceof Integer) {
                Integer i = (Integer) value;
                if (i > 0) {
                    return i;
                }
            }
            throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                propName + "'. Must be an " + toString());
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return INT;
        }
        
        @Override
        public String toString() {
            return "integer greater than zero or \"off\"";
        }
    }
    
    private static class PFloat extends CPTFloat {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            Double dvalue = (Double) super.checkValue(propName, value, loc);
            if (dvalue < 0) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                propName + "'. Must be a " + toString());
            }
            return dvalue;
        }
        
        @Override
        public String toString() {
            return "positive number";
        }
    }
    
    private static class CPTFloat extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                }
                catch (NumberFormatException e) {
                    throw cannotConvert(loc, propName, value, "number");
                }
            }
            else if (value instanceof Double) {
                return value;
            }
            else {
                throw cannotConvert(loc, propName, value, "number");
            }
        }

        @Override
        public ConfigPropertyType<?> getBaseType() {
            return FLOAT;
        }
    }
    
    public static class Interval extends CPTFloat {
        private double l, h;
        
        public Interval(double l, double h) {
            this.l = l;
            this.h = h;
        }
        
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            Double dvalue = (Double) super.checkValue(propName, value, loc);
            if (dvalue < l || dvalue > h) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Must be a " + toString());
            }
            return dvalue;
        }

        @Override
        public String toString() {
            return "floating point number in the interval [" + l + ", " + h + "]";
        }
    }
    
    private static class CPTTime extends ConfigPropertyType<String> {
        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            try {
                WallTime.timeToSeconds(value);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid time value '" + value + "' for property '" + 
                    propName + "'. Mist be a " + toString());
            }
            return value;
        }
        
        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }
        
        @Override
        public String toString() {
            return "string in one of the formats MM, HH:MM, or HH:MM:SS";
        }
    }
    
    private static class PortRange extends ConfigPropertyType<String> {
        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            String[] els = value.split(",\\s*");
            if (els.length == 2) {
                try {
                    Integer.parseInt(els[0]);
                    Integer.parseInt(els[1]);
                    return value;
                }
                catch (NumberFormatException e) {
                }
            }
            throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Must be a " + toString());
        }
        
        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }
        
        @Override
        public String toString() {
            return "port range in the format 'port1, port2'";
        }
    }
    
    private static class CPTFile extends ConfigPropertyType<String> {
        @Override
        public Object checkValue(String propName, String value, ConfigOrigin loc) {
            File f = new File(value);
            if (!f.exists()) {
                throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. File does not exist.");
            }
            return value;
        }
                
        @Override
        public ConfigPropertyType<?> getBaseType() {
            return STRING;
        }

        @Override
        public String toString() {
            return "file path";
        }
    }
    
    private static class CPTObject extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            return value;
        }
        
        
        @Override
        public ConfigPropertyType<?> getBaseType() {
            return OBJECT;
        }
        
        @Override
        public String toString() {
            return "object";
        }
    }
    
    private static class StringList extends ConfigPropertyType<Object> {
        @Override
        public Object checkValue(String propName, Object value, ConfigOrigin loc) {
            if (value instanceof List) {
                List<?> l = (List<?>) value;
                boolean allStrings = true;
                for (Object o : l) {
                    if (!(o instanceof String)) {
                        allStrings = false;
                    }
                }
                if (allStrings) {
                    return value;
                }
            }
            else if (value instanceof String) {
                // also allow comma separated strings in a string
                return Arrays.asList(((String) value).split(",\\s*"));
            }
            throw new IllegalArgumentException(location(loc) + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Must be a " + toString());
        }
        
        @Override
        public ConfigPropertyType<?> getBaseType() {
            return OBJECT;
        }
        
        @Override
        public String toString() {
            return "list of strings";
        }
    }
    
    private static String location(ConfigOrigin loc) {
        return loc.filename() + ":" + loc.lineNumber();
    }
}
