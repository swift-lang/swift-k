//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digester {
    
    public static String computeMD5(File f) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buf = new byte[1024];
        FileInputStream fis = new FileInputStream(f);
        int read = fis.read(buf);
        while (read != -1) {
            md.update(buf, 0, read);
            read = fis.read(buf);
        }
        byte[] digest = md.digest();
        return hex(digest);
    }

    public static String hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(hex((b[i] & 0xf0) >> 4));
            sb.append(hex(b[i] & 0x0f));
        }
        return sb.toString();
    }

    private static char hex(int nibble) {
        if (nibble < 10) {
            return (char) (nibble + '0');
        }
        else {
            return (char) (nibble - 10 + 'a');
        }
    }
}
