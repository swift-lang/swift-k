
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/**
 * A Name Generator generates unique names of the datatype
 * string. This is useful to automatically generate unique names for
 * GridFaces so they can be distingusched mere easily and conveniently
 * from each other.
 *
 */

public interface GridFaceLabelGenerator {

    /**
     * Creates a unique name and puts it between prefix and postfix.
     *
     * @param prefix a <code>String</code> that is included at the beginning.
     * @param postfix a <code>String</code> that is appended.
     * @return a <code>String</code> that is unique.
     */
    String get(String prefix, String postfix);

    /**
     * Creates a unique name
     *
     * @return a <code>String</code> that is unique
     */
    String get();

    /**
     * Sets a prefix that will be added every time the get method is
     * called. The default is "".
     *
     * @param prefix a <code>String</code> the string to be included.
     */
    public void setPrefix(String prefix);

    /**
     * Appends a sting postfix every time the get method is
     * called. The default is "".
     *
     * @param postfix a <code>String</code> value
     */
    public void setPostfix(String postfix);

}
