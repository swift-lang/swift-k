
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Identity;

/**
 * An implementation of the <code>Identity</code> interface. It
 * calculates a unique value for this <code>Identity</code>by
 * assigning it the value of a static counter that was initialized by
 * the current time of day in milliseconds.
 */
public class IdentityImpl implements Identity {
    static Logger logger = Logger.getLogger(IdentityImpl.class.getName());
    private URI uri = null;
    private final String scheme = "urn";
    private String nameSpace = "cog";
    private long value;
    private String schemeSpecific = null;
    private static long count = System.currentTimeMillis();

    /**
     * The default constructor. Assigns a default namespace to this
     * identity as <code>cog</code>.
     */
    public IdentityImpl() {
        synchronized (this) {
            this.value = count++;
        }
        createURI();
    }

    /**
    * Instantiates an <code>Identity</code> with the given namespace.
     */
    public IdentityImpl(String namespace) {
        synchronized (this) {
            this.value = count++;
        }
        this.nameSpace = namespace;
        createURI();
    }

    /**
     * Makes a shallow copy of the given
     * <code>Identity</code>. Instantiates an <code>Identity</code> by
     * copying the namespace and value from the given
     * <code>Identity</code>.
     */
    public IdentityImpl(Identity identity) {
        this.nameSpace = identity.getNameSpace();
        this.value = identity.getValue();
        createURI();
    }

    public void setNameSpace(String namespace) {
        this.nameSpace = namespace;
        createURI();
    }

    public String getNameSpace() {
        return this.nameSpace;
    }

    public void setValue(long value) {
        this.value = value;
        createURI();
    }

    public long getValue() {
        return this.value;
    }

    public boolean equals(Identity id) {
        return this.toString().equalsIgnoreCase(id.toString());
    }

    public boolean equals(Object object) {
        return this.toString().equalsIgnoreCase(((Identity) object).toString());
    }

    public int hashCode() {
        return (int) this.value;
    }

    public String toString() {
        return this.uri.toString();
    }

    private void createURI() {
        this.schemeSpecific = this.nameSpace + "-" + this.value;
        try {
            this.uri = new URI(this.scheme + ":" + this.schemeSpecific);
        } catch (Exception e) {
            logger.error("Error while creating identity", e);
        }
    }

}
