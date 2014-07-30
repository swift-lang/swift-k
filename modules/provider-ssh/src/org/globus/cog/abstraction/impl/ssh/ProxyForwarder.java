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
 * Created on Sep 18, 2012
 */
package org.globus.cog.abstraction.impl.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.SigningPolicy;
import org.globus.gsi.TrustedCertificates;



public abstract class ProxyForwarder {
    public static final Logger logger = Logger.getLogger(ProxyForwarder.class);
    
    public static final String PROXY_PREFIX = "sshproxy";
    public static final String CA_PREFIX = "sshCAcert";

    
    public static class Key {
        public final ConnectionID connectionId;
        public final int delegationType;

        public Key(ConnectionID connectionId, int delegationType) {
            this.connectionId = connectionId;
            this.delegationType = delegationType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key k = (Key) obj;
                return k.connectionId.equals(connectionId)
                        && k.delegationType == delegationType;
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return connectionId.hashCode() + delegationType;
        }
    }

    public static class Info {
        public final String proxyFile, caCertFile;
        public final long expirationTime;

        public Info(String proxyFile, String caCertFile, long expirationTime) {
            this.proxyFile = proxyFile;
            this.caCertFile = caCertFile;
            this.expirationTime = expirationTime;
        }
    }


    protected abstract Key getKey(int type);

    protected abstract Info writeCredential(GlobusCredential cred) throws InvalidSecurityContextException;
    
    protected X509Certificate getCaCert(X509Certificate userCert) throws InvalidSecurityContextException {
        TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
            
        X509Certificate caCert = tc.getCertificate(userCert.getIssuerDN().getName());
        if (caCert == null) {
            logger.warn("Cannot find root CA certificate for proxy");
            logger.warn("DNs of trusted certificates:");
            X509Certificate[] roots = tc.getCertificates();
            for (X509Certificate root : roots) {
                logger.warn("\t" + root.getSubjectDN());
            }
            throw new InvalidSecurityContextException("Failed to find root CA certificate (" + userCert.getIssuerDN().getName() + ")");
        }
        else {
            return caCert;
        }
    }
    
    protected SigningPolicy getSigningPolicy(X509Certificate userCert) {
        TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
        return tc.getSigningPolicy('/' + userCert.getIssuerDN().getName().replace(',', '/'));
    }
    
    protected void streamCopy(OutputStream out, InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        while (len != -1) {
            out.write(buf, 0, len);
            len = in.read(buf);
        }
        out.close();
        in.close();
    }
}
