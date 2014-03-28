//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2014
 */
package org.griphyn.vdl.mapping.file;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.globus.cog.util.Base64;

public abstract class AbstractFileNameElementMapper implements FileNameElementMapper {
    public static final Logger logger = Logger.getLogger(AbstractFileNameElementMapper.class);

    @Override
    public String mapIndex(Object key, int pos) {
        String token;
        String f;
        if (key instanceof Integer) {
            token = key.toString();
            f = mapIndex(((Integer) key).intValue(), pos);
        }
        else if (key instanceof Double) {
            token = Double.toHexString(((Double) key).doubleValue());
            f = mapField(token);
        }
        else {
            MessageDigest md = getDigest();
            byte[] buf = md.digest(key.toString().getBytes());
            token = encode(buf);
            f = mapField(token);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Mapping path component to " + token);
        }
        return f;
    }

    private String encode(byte[] buf) {
        buf = Base64.encode(buf);
        char[] c = new char[buf.length];
        for (int i = 0; i < buf.length; i++) {
            c[i] = (char) buf[i];
        }
        return String.copyValueOf(c);
    }

    private MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("JVM error: SHA-1 not available");
        }
    }

}
