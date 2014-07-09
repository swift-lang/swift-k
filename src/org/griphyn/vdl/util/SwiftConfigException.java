//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 5, 2014
 */
package org.griphyn.vdl.util;

import com.typesafe.config.ConfigOrigin;

public class SwiftConfigException extends RuntimeException {
    private ConfigOrigin loc;
    
    public SwiftConfigException(ConfigOrigin loc, String message) {
        super((loc.filename() == null ? loc.description() : loc.filename() + ":" + loc.lineNumber()) + " " + message);
    }
}
