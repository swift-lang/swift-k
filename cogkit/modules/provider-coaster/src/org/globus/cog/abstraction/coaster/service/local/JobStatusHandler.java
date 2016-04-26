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
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RemoteException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class JobStatusHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(JobStatusHandler.class);
    
    public static final String NAME = "JOBSTATUS";
    
    public void requestComplete() throws ProtocolException {
        try {
            String jobId = getInDataAsString(0);
            int status = getInDataAsInt(1);
            int code = getInDataAsInt(2);
            String message = getInDataAsString(3);
            
            String out = null, err = null;
            
            Status s = new StatusImpl();
            s.setStatusCode(status);
            if (status == Status.FAILED && code != 0) {
            	if (message != null && !message.equals("")) {
            		s.setException(new JobException(message, code));
            	}
            	else {
            	    s.setException(new JobException(code));
            	}
            }
            if (status == Status.FAILED || status == Status.COMPLETED) {
                switch (getInDataSize()) {
                    case 8:
                        // outs + exception
                        s.setException(getException(getInData(7)));
                    case 7:
                        out = getInDataAsString(5);
                        err = getInDataAsString(6);
                        break;
                    case 6:
                        // just exception
                        s.setException(getException(getInData(5)));
                }
            }
            if (message != null && !message.equals("")) {
                s.setMessage(message);
            }
            long ts = this.getInDataAsLong(4);
            if (ts == 0) {
                s.setTime(new Date());
            }
            else {
                s.setTime(new Date(ts));
            }
            Identity id = IdentityImpl.parse(jobId);
            NotificationManager.getDefault().notificationReceived(id, s, out, err);
            sendReply("OK");
        }
        catch (Exception e) {
            throw new ProtocolException("Could not deserialize job status", e);
        }
    }

    private Exception getException(byte[] inData) {
        // 0xaced is the magic sequence for the java serialization protocol
        if (inData.length > 2 && inData[0] == (byte) 0xac && inData[1] == (byte) 0xed) {
            // serialized Java Object
            try {
                ByteArrayInputStream is = new ByteArrayInputStream(inData);
                ObjectInputStream oos = new ObjectInputStream(is);
                Object o = oos.readObject();
                if (o instanceof Exception) {
                    return new RemoteException((Exception) o);
                }
                else {
                    return new RuntimeException(o.toString());
                }
            }
            catch (Exception e) {
                logger.info("Could not deserialize job status exception", e);
                return new RuntimeException("Error deserializing job status exception");
            }
        }
        else if (inData.length == 0) {
            return null;
        }
        else {
            return new RuntimeException(new String(inData));
        }
    }
}
