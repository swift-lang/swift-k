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
 * Created on Jul 8, 2011
 */
package org.globus.cog.abstraction.coaster.service;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class ResourceUpdateHandler extends RequestHandler { 
    public static Logger logger = Logger.getLogger(ResourceUpdateHandler.class);
    
    @Override
    public void requestComplete() throws ProtocolException {
        LocalService ls = (LocalService) getChannel().getService();
        if (ls == null) {
            // getting this on a client channel, so just log this
            logger.info(getInDataAsString(0) + ": " + getInDataAsString(1));
        }
        else {
            ls.resourceUpdated(getChannel(), 
                getInDataAsString(0), getInDataAsString(1));
        }
        sendReply("OK");
    }
}
