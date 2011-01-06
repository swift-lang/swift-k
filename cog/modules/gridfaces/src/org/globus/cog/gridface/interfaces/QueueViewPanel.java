
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;


/** Maintain the view panel for the queue. Short view/Detailed view
provides a predefined set of components. Custom view allows the user to 
select what is to be displayed in the queue view panel.
**/

public interface QueueViewPanel extends GridFace{

    //View type = 1. Short view
    //View type = 2. Detailed view
    //View type = 3. Custom view

    public void setViewPanel(int type);
       
    /** Add component to the queue view panel**/
    public void addViewComponent(String name);
    
    /** Remove component from the view panel **/
    public void removeViewComponent(String name);


    /** Add component to the queue view panel**/
    public void addViewComponents(String[] name);
    
    /** Remove component from the view panel **/
    public void removeViewComponents(String[] name);

    /** register the component with the panel for optional display. 
	Not runtime **/
    public void register(Object name);

    /** Get all components (names to be displayed for selection by user) 
	registered with the current panel **/
    public String[] getAllComponents();
}

/**
setAttribute (String name, String value)
String getAttribute(String name)
Enumeration getAttributes()
int NumberOfAttributes()
*/