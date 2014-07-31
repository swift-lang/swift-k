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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.mpichg2;

import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;

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
        Authorization authorization = (Authorization) getAttribute("authorization");
        if (authorization == null){
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
            return GlobusSecurityContextImpl.FULL_DELEGATION;
        }
        return value.intValue();
    }
}