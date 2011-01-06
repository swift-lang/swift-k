// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt4_0_0;

import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.gsi.gssapi.auth.Authorization;

public class GlobusSecurityContextImpl extends SecurityContextImpl {
    public static final int XML_ENCRYPTION = 1;
    public static final int XML_SIGNATURE = 2;

    public static final int NO_DELEGATION = 0;
    public static final int FULL_DELEGATION = 1;
    public static final int PARTIAL_DELEGATION = 2;

    public void setAuthorization(Authorization authorization) {
        setAttribute("authorization", authorization);
    }

    public Authorization getAuthorization() {
        return (Authorization) getAttribute("authorization");
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
}