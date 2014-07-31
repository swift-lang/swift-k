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
 * Created on Apr 22, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ScriptManager {
    public static final File scriptDir =
            new File(System.getProperty("user.home") + 
                File.separator + ".globus" + 
                File.separator + "coasters");
    public static final String SCRIPT = "worker.pl";
    
    public static File writeScript() throws IOException {
        scriptDir.mkdirs();
        if (!scriptDir.exists()) {
            throw new IOException("Failed to create script dir (" + scriptDir + ")");
        }
        File script = File.createTempFile("cscript", ".pl", scriptDir);
        if (! "persistent".equals(System.getProperty("coaster.worker.script")))
            script.deleteOnExit();
        InputStream is = ScriptManager.class.getClassLoader().getResourceAsStream(SCRIPT);
        if (is == null) {
            throw new IOException("Could not find resource in class path: " + SCRIPT);
        }
        FileOutputStream fos = new FileOutputStream(script);
        byte[] buf = new byte[1024];
        int len = is.read(buf);
        while (len != -1) {
            fos.write(buf, 0, len);
            len = is.read(buf);
        }
        fos.close();
        is.close();
        return script;
    }
}
