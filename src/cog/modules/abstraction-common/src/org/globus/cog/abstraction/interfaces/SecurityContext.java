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