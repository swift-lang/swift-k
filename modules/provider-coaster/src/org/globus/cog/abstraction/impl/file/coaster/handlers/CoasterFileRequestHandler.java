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
 * Created on Sep 29, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;

import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public abstract class CoasterFileRequestHandler extends RequestHandler {
    // private static final String HOME = System.getProperty("user.home");
    private static final String CWD = new File(".").getAbsolutePath();

    public static File normalize(RemoteFile rf) {
        if (rf.isAbsolute()) {
            return new File(rf.getPath());
        }
        else {
            return new File(CWD, rf.getPath());
        }
    }
    
    public static File normalize(String path) {
        return normalize(new RemoteFile(path));
    }

    protected String getProtocol(String file) {
        int index = file.indexOf(':');
        if (index == -1) {
            return "file";
        }
        else {
            return file.substring(0, index);
        }
    }

    protected void sendReply() throws ProtocolException {
        NotificationManager.getDefault().notIdle();
        super.sendReply();
    }
}
