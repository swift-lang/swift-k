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
 * Created on Nov 10, 2012
 */
package org.griphyn.vdl.engine;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

public class CompilerUtils {
    public static String getLine(Node n) {
        if (n == null || n.getAttributes() == null) {
            return "unknown";
        }
        Node src = n.getAttributes().getNamedItem("src");
        if (src == null) {
            return getLine(n.getParentNode());
        }
        else {
            String loc = src.getNodeValue();
            if (loc == null || loc.length() == 0) {
                return "unknown";
            }
            else {
                return loc.substring(loc.indexOf(' ') + 1);
            }
        }
    }

    public static String getLine(XmlObject src) {
        if (src == null) {
            return null;
        }
        return getLine(src.getDomNode());
    }
    
    public static String info(XmlObject src) {
        return src.getDomNode().getLocalName() + ", line " + getLine(src);
    }
}
