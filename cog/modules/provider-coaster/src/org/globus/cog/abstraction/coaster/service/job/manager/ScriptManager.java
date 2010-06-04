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
