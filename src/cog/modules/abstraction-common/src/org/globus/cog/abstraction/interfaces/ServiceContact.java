// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.io.Serializable;

/**
 * This interfaces abstracts the endpoint service handle of remote Grid
 * services.
 */
public interface ServiceContact extends Serializable {
    /**
     * Sets the host element of this <code>ServiceContact</code>
     */
    public void setHost(String host);

    /**
     * Returns the host element of this <code>ServiceContact</code>
     */
    public String getHost();

    /**
     * Sets the port element of this <code>ServiceContact</code>
     */
    public void setPort(int port);

    /**
     * Returns the port element of this <code>ServiceContact</code>
     */
    public int getPort();

    /**
     * Sets the entire contact string of this <code>ServiceContact</code>
     */
    public void setContact(String contact);

    /**
     * Returns the entire contact string of this <code>ServiceContact</code>
     */
    public String getContact();
}