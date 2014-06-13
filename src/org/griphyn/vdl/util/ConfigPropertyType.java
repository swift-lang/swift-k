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
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class ConfigPropertyType {
    public static final ConfigPropertyType BOOLEAN = choices("true", "false");
    public static final ConfigPropertyType ONOFF = choices("on", "off");
    public static final ConfigPropertyType STRING = new CPTString();
    public static final ConfigPropertyType INT = new Int();
    public static final ConfigPropertyType FLOAT = new CPTFloat();
    public static final ConfigPropertyType FILE = new CPTFile();
    
    public static ConfigPropertyType choices(String... values) {
        return new Choices(values);
    }
    
    public abstract void checkValue(String propName, String value, String source);
    
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
    
    private static class Choices extends ConfigPropertyType {
        private SortedSet<String> choices;
        
        public Choices(String... values) {
            choices = new TreeSet<String>();
            for (String value : values) {
                choices.add(value);
            }
        }

        @Override
        public void checkValue(String propName, String value, String source) {
            if (!choices.contains(value)) {
                throw new IllegalArgumentException(source + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Valid values are: " + pp(choices));
            }
        }
    }
    
    private static class CPTString extends ConfigPropertyType {
        @Override
        public void checkValue(String propName, String value, String source) {
            // all values accepted
        }
    }
    
    private static class Int extends ConfigPropertyType {
        @Override
        public void checkValue(String propName, String value, String source) {
            try {
                Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException(source + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Must be an integer");
            }
        }
    }
    
    private static class CPTFloat extends ConfigPropertyType {
        @Override
        public void checkValue(String propName, String value, String source) {
            try {
                Double.parseDouble(value);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException(source + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. Must be a floating point number.");
            }
        }
    }
    
    private static class CPTFile extends ConfigPropertyType {
        @Override
        public void checkValue(String propName, String value, String source) {
            File f = new File(value);
            if (!f.exists()) {
                throw new IllegalArgumentException(source + ":\n\tInvalid value '" + value + "' for property '" + 
                    propName + "'. File does not exist.");
            }
        }
    }
}
