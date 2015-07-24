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
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.commands.Command;

public class CleanupCommand extends Command {
    public static final Logger logger = Logger.getLogger(CleanupCommand.class);
    
    public static final String NAME = "CLEANUP";
    
    private CleanUpSet cleanupSet;
    
    public CleanupCommand(CleanUpSet cleanupSet) {
        super(NAME);
        this.cleanupSet = cleanupSet;
    }

    public void send() throws ProtocolException {
        try {
            serialize();
        }
        catch (Exception e) {
            throw new ProtocolException("Could not serialize job specification", e);
        }
        super.send();
    }

    protected void serialize() throws IOException {
        StringBuilder sb = new StringBuilder();
        
        for (String e : cleanupSet) {
            add(sb, e);
        }
        
        String out = sb.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("Cleanup data: " + out);
        }
        
        byte[] bytes = out.getBytes(UTF8);
        addOutData(bytes);
    }
    
    @SuppressWarnings("fallthrough")
    public static void add(final StringBuilder sb, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\n':
                    c = 'n';
                case '\\':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }

        sb.append('\n');
    }

    
    private static final Charset UTF8 = Charset.forName("UTF-8");
}
