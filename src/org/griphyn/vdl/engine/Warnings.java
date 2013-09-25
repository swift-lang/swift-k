//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 10, 2012
 */
package org.griphyn.vdl.engine;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

public class Warnings {
    public static final Logger logger = Logger.getLogger(Warnings.class);
    
    private static Set<String> warnings = new HashSet<String>();
    
    public static void warn(XmlObject obj, String msg) {
        if (!warnings.contains(msg)) {
            warnings.add(msg);
            msg = "Warning: " + msg + ", at " + CompilerUtils.getLine(obj.getDomNode());
            logger.info(msg);
            System.err.println(msg);
        }
    }
    
    public static void warn(String msg) {
        if (!warnings.contains(msg)) {
            warnings.add(msg);
            msg = "Warning: " + msg;
            logger.info(msg);
            System.err.println(msg);
        }
    }
}
