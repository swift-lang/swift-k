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

public class DuplicateMappingChecker {
    public static final Logger logger = Logger.getLogger(DuplicateMappingChecker.class);
    
    private final Map<PhysicalFormat, Entry> map;
    
    public DuplicateMappingChecker() {
        map = new HashMap<PhysicalFormat, Entry>();
    }
    
    private static class Entry {
        private DSHandle write;
        private List<DSHandle> read;
        
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
        Entry e = getEntry(f);
        if (e.write != null) {
            warn("Duplicate mapping found:\n\t" + 
                getVarInfo(h) + " is used to read from " + f + "\n\t" + 
                getVarInfo(e.write) + " is used to write to " + f);
        }
        if (e.read == null) {
            e.read = new LinkedList<DSHandle>();
        }
        e.read.add(h);
    }

    public synchronized void addWrite(PhysicalFormat f, DSHandle h) {
        Entry e = getEntry(f);
        if (e.write != null) {
            warn("Duplicate mapping found:\n\t" + 
                getVarInfo(h) + " and " + getVarInfo(e.write) + " are both used to write to " + f);
        }
        if (e.read != null) {
            warn("Duplicate mapping found:\n\t" + 
                getVarInfo(e.write) + " is used to write to " + f + "\n\t" + 
                "The following variables(s) are also used to read from " + f + ":" + getVarInfos(e.read));
        }
        e.write = h;
    }
    
    private void warn(String s) {
        if (logger.isInfoEnabled()) {
            logger.info(s);
        }
        System.err.println(s);
    }

    private String getVarInfos(List<DSHandle> l) {
        StringBuilder sb = new StringBuilder();
        for (DSHandle h : l) {
            sb.append("\n\t\t");
            sb.append(getVarInfo(h));
        }
        return sb.toString();
    }

    private String getVarInfo(DSHandle h) {
        if (h instanceof AbstractDataNode) {
            AbstractDataNode a = (AbstractDataNode) h;
            return a.getDisplayableName() + " (line " + a.getDeclarationLine() + ")";
        }
        else {
            return String.valueOf(h);
        }
    }
}
