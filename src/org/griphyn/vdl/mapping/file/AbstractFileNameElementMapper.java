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
        int len;
        if (buf[buf.length - 1] == '=') {
            len = buf.length - 1;
        }
        else {
            len = buf.length;
        }
        char[] c = new char[len];
        for (int i = 0; i < len; i++) {
            c[i] = (char) buf[i];
            if (c[i] == '/') {
                c[i] = '-';
            }
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
