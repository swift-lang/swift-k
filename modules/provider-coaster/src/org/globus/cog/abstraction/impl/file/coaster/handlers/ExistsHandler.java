//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 26, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;

public class ExistsHandler extends CoasterFileRequestHandler {
	public static final Logger logger = Logger.getLogger(ExistsHandler.class);

    public void requestComplete() throws ProtocolException {
    	File f = normalize(getInDataAsString(0));
    	if (logger.isInfoEnabled()) {
    		logger.info("Checking if " + f.getAbsolutePath() + " exists");
    	}
    	if (f.exists()) {
    	    addOutData(true);
    	}
    	else {
    	    addOutData(false);
    	}
    	sendReply();
    }

}
