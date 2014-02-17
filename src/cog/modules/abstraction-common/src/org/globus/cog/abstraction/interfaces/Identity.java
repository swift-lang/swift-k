// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

/**
 * A representation of a unique identity held by various cog-core objects. An
 * <code>Identity</code> is a {@link URI}, collectively composed of a
 * <code>namespace</code> and <code>value</code>. A sample
 * <code>Identity</code> looks as follows:
 * <p>
 * urn:&lt;namespace&gt;:&lt;value&gt;
 */
public interface Identity {

    /**
     * Sets the namespace for this <code>Identity</code>.
     * 
     * @param namespace
     *            a string representing the namespace for this
     *            <code>Identity</code>.
     */
    public void setNameSpace(String namespace);

    /**
     * Returns the namespace for this <code>Identity</code>.
     * 
     * @return the namespace for this <code>Identity</code>
     */
    public String getNameSpace();

    /**
     * Sets the value for this <code>Identity</code>.
     * 
     * @param value
     *            a String value representing the value for this
     *            <code>Identity</code>.
     */
    public void setValue(String value);

    /**
     * Returns the value for this <code>Identity</code>.
     * 
     * @return the value for this <code>Identity</code>
     */
    public String getValue(); 
}