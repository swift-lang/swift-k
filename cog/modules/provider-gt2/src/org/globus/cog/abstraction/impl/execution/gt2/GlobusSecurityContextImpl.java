// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class GlobusSecurityContextImpl extends SecurityContextImpl implements Delegation {
    public static final int XML_ENCRYPTION = 1;
    public static final int XML_SIGNATURE = 2;
    
    public static final int DEFAULT_CREDENTIAL_REFRESH_INTERVAL = 30000;
    private static GSSCredential cachedCredential;
    private static long credentialTime;

    
    public void setAuthorization(Authorization authorization) {
        setAttribute("authorization", authorization);
    }
    
    public Authorization getAuthorization() {
        Authorization authorization = (Authorization) getAttribute("authorization");
        if (authorization == null) {
            authorization = HostAuthorization.getInstance();
        }
        return authorization;
    }

    public void setXMLSec(int xml_security) {
        setAttribute("xml_security", new Integer(xml_security));
    }

    public int getXMLSec() {
        Integer value = (Integer) getAttribute("xml_security");
        if (value == null) {
            return GlobusSecurityContextImpl.XML_SIGNATURE;
        }
        return value.intValue();
    }

    public void setDelegation(int delegation) {
        setAttribute("delegation", new Integer(delegation));
    }

    public int getDelegation() {
        Integer value = (Integer) getAttribute("delegation");
        if (value == null) {
            return GlobusSecurityContextImpl.PARTIAL_DELEGATION;
        }
        return value.intValue();
    }
    
    public GSSCredential getDefaultCredential() throws InvalidSecurityContextException {
        return _getDefaultCredential();
    }
    
    public static GSSCredential _getDefaultCredential() throws InvalidSecurityContextException {
        synchronized (GlobusSecurityContextImpl.class) {
            if (cachedCredential == null
                    ||
                    (System.currentTimeMillis() - credentialTime) > DEFAULT_CREDENTIAL_REFRESH_INTERVAL) {
                credentialTime = System.currentTimeMillis();
                GSSManager manager = ExtendedGSSManager.getInstance();
                try {
                    cachedCredential = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
                }
                catch (GSSException e) {
                    throw new InvalidSecurityContextException(e);
                }
            }
            return cachedCredential;
        }
    }
}