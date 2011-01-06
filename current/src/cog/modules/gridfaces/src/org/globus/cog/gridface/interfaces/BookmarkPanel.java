
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/*
 +--------------------------------+
 | Location: ___________________  |
 +--------------------------------+
 |  + People                      |
 |         - Gregor von Laszewski |
 |  + Machines                    |
 |       - hot.mcs.anl.gov        |
 |  + Workflows                   |
 |       - mywork                 |
 |  + Files                       |
 |  + Project                     |
 + -------------------------------+

 * This panel is a convenient graphical component that lets a user to
 * organize important resources as part of a bookmark like feature. It
 * allows theu user to create arbitrary groups or categories under
 * which he can store objects refering to resources and services on
 * the Grid. Clicking on the resource will return the apropiate
 * Object. Advanced components can be developed with the help of such
 * a bookmark tool as a selector is available. Keywords are an
 * alternative way to create groups. An Object can have multiple
 * keywords.
*/

public interface BookmarkPanel extends GridFace {

  
    /**
     * Adds a group to the bokmarks.
     *
     * @param name a <code>String</code> that contanins the group name.
     */
    public void addGroup (String name);

    /**
     * Removes a group to the bokmarks. If the child is used in another group, the child will not be deleted.
     *
     * @param name a <code>String</code> that contanins the group name.
     */
    public void removeGroup (String name);

    /**
     * returns all object within the group.
     *
     * @param name a <code>String</code> that contanins the group name.
     */
    public Object[] getGroup (String name);


    /**
     * Adds a keyword to an object. (we need to also have keywords for groups)
     *
     * @param name a <code>String</code> that contanins the group name.
     */
    public void addKeyword (String key);

    /**
     * Removes a keyword from an object. If the child is used in another group, the child will not be deleted.
     *
     * @param key a <code>String</code> that contanins the group name.
     */
    public void removeKeyword (String key, Object object);

    /**
     * returns all object within the keyword.
     *
     * @param name a <code>String</code> that contanins the group name.
     */
    public Object[] getKeyword (String key);

    /**
     * Returns the selected object or group.
     *
     */
    public void getSelected();

    /**
     * Sets the selected object.
     *
     * @param name a <code>String</code> value that identifies the object to be selected.
     */
    public void setSelected(String name);

    // adds an object with the anme and keywords the the specified group
    public void addObject(String group, String name, String [] keywords, Object o);

    // removes a named object from the group
    public void removeObject(String group, String name);

    // removes a named object from all groups
    public void removeObject(String name);
    
}

