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
 * Created on Nov 18, 2012
 */
package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.util.SwiftConfig;

public class DuplicateMappingChecker {
    public static final Logger logger = Logger.getLogger(DuplicateMappingChecker.class);
    
    public boolean enabled = true;
    
    private final Map<PhysicalFormat, Entry> map;
    
    public DuplicateMappingChecker(SwiftConfig conf) {
        enabled = conf.isMappingCheckerEnabled();
        map = new HashMap<PhysicalFormat, Entry>();
    }
    
    private static class Info {
        private final String name;
        private final Integer line;
        
        public Info(String name, Integer line) {
            this.name = name;
            this.line = line;
        }
        
        public String toString() {
            if (line == null) {
                return name;
            }
            else {
                return name + " (line " + line + ")";
            }
        }
    }
    
    private static class Entry {
        private Info write;
        private List<Info> read;
    }
    
    private Entry getEntry(PhysicalFormat f) {
        Entry e = map.get(f);
        if (e == null) {
            e = new Entry();
            map.put(f, e);
        }
        return e;
    }

    public synchronized void addRead(PhysicalFormat f, DSHandle h) {
        if (!enabled) {
            return;
        }
        Entry e = getEntry(f);
        if (e.write != null) {
            warn("WARNING: duplicate mapping found:\n\t" + 
                formatInfo(getInfo(h)) + " is used to read from " + f + "\n\t" + 
                formatInfo(e.write) + " is used to write to " + f);
        }
        if (e.read == null) {
            e.read = new LinkedList<Info>();
        }
        e.read.add(getInfo(h));
    }

    private Info getInfo(DSHandle h) {
        if (h instanceof AbstractDataNode) {
            AbstractDataNode a = (AbstractDataNode) h;
            return new Info(a.getDisplayableName(), a.getRoot().getLine());
        }
        else {
            return new Info(String.valueOf(h), null);
        }
    }

    public synchronized void addWrite(PhysicalFormat f, DSHandle h) {
        if (!enabled) {
            return;
        }
        if (f == null) {
            /*
             *  sometimes only a sub-set of a complex structure is mapped and used
             *  such as, for example, in 0755-ext-mapper.swift
             *  
             *  In such cases, don't complain about multiple things being mapped
             *  to null
             */
            return;
        }
        Entry e = getEntry(f);
        if (e.write != null) {
            warn("WARNING: duplicate mapping found:\n\t" + 
                formatInfo(getInfo(h)) + " and " + formatInfo(e.write) + " are both used to write to " + f);
        }
        if (e.read != null) {
            warn("WARNING: duplicate mapping found:\n\t" + 
                formatInfo(e.write) + " is used to write to " + f + "\n\t" + 
                "The following variables(s) are also used to read from " + f + ":" + formatInfos(e.read));
        }
        e.write = getInfo(h);
    }
    
    private void warn(String s) {
        if (logger.isInfoEnabled()) {
            logger.info(s);
        }
        System.err.println(s);
    }

    private String formatInfos(List<Info> l) {
        StringBuilder sb = new StringBuilder();
        for (Info h : l) {
            sb.append("\n\t\t");
            sb.append(formatInfo(h));
        }
        return sb.toString();
    }

    private String formatInfo(Info i) {
        return i.toString();
    }
}
