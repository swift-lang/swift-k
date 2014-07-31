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

package org.globus.cog.abstraction.interfaces;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.ietf.jgss.GSSCredential;

/**
 * This interface abstracts the security credentials in the abstractions
 * framework. For remote Globus services, the security credential can be a valid
 * {@link GSSCredential}. A <code>null</code> credential for remote Globus
 * services indicates the default proxy certificate.
 */
public interface SecurityContext {
    /**
     * Sets the credentials for this <code>SecurityContext</code>
     */
    public void setCredentials(Object credentials);

    /**
     * Returns the credentials for this <code>SecurityContext</code>
     */
    public Object getCredentials();

    public void setAlias(String alias);

    public String getAlias();

    public void setCredentials(Object credentials, String alias);

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);
    
    /**
     * If this security context supports default credentials, return them;
     * else return <code>null</code>
     */
    public Object getDefaultCredentials();
    
    public void setServiceContact(ServiceContact serviceContact);
    
    public ServiceContact getServiceContact();
}