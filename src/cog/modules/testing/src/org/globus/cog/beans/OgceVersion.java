
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.beans;

/** This class allows to query the current version of the released software.
 * The release number is assambled by MAJOR.MINOR.PATCH
 */
public class OgceVersion {

    /** The major release number */
    public static final int MAJOR = 1;

    /** The minor release number */
    public static final int MINOR = 1;

    /** The patchlevel of the current release */
    public static final int PATCH = 0;
    
    /**  Release cycle*/
    public static final String RELEASECYCLE = "a";
    /** Retruns the current version as string in the form MAJOR.MINOR.PATCH
     */
    public static String getVersion() {
	return getMajor() + "." + getMinor() + "." + getPatch()+ getReleaseCycle();
    }

    /** Returns the major release number
     * 
     * @return the major release
     */
    public static int getMajor() {
	return MAJOR;
    }
    public static String getReleaseCycle() {
	return RELEASECYCLE;
    }
    
    /** Returns the minor release number
     * 
     * @return the minor release number
     */
    public static int getMinor() {
	return MINOR;
    }
    
    /** Returns the patch level
     * 
     * @return the patch level
     */
    public static int getPatch() {
	return PATCH;
    }
    
    /** Returns the version for the Java CoG Kit as a readble string.
     * 
     * @param args 
     */
    public static void main(String [] args) {
	System.out.println("Java CoG version: " + getVersion());
    }

}
