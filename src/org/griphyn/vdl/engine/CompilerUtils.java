//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 10, 2012
 */
package org.griphyn.vdl.engine;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

public class CompilerUtils {
    public static String getLine(Node n) {
        if (n == null) {
            return "line unknown";
        }
        Node src = n.getAttributes().getNamedItem("src");
        if (src == null) {
            return getLine(n.getParentNode());
        }
        else {
            return src.getNodeValue();
        }
    }

    public static String getLine(XmlObject src) {
        if (src == null) {
            return null;
        }
        String loc = src.getDomNode().getAttributes().getNamedItem("src").getNodeValue();
        if (loc == null) {
            return null;
        }
        else {
            return loc.substring(loc.indexOf(' ') + 1);
        }
    }
}
