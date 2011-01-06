//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.globus.cog.gridface.impl.desktop.interfaces.AccessAttributes;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences;

public class AttributesHolder implements AccessAttributes,AccessPreferences{
	private Hashtable attributes = new Hashtable();
	

	
	public void clear(){
		this.attributes.clear();
	}

    /**
     * set attributes for the given command
     */
    public void setAttribute(String name, Object value) {
    	if(value!=null){
    		this.attributes.put(name.toLowerCase(), value);
    	}
    }

    /** Get the attribute with given name */
    public Object getAttribute(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    /** Get all attributes in the form of a hash table */
    public Hashtable getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Hashtable attribs){
    	this.attributes = attribs;
    }
    
    public void removeAttribute(String name){
    	this.attributes.remove(name.toLowerCase());
    }
    
    
	public void loadPreferences(Preferences startNode) {
		try{
			String[] children = startNode.keys();
			for (int i = 0; i < children.length; i++) {
				if(children[i].startsWith(SUFFIX_TAG)){
					this.setAttribute(children[i].substring(SUFFIX_TAG.length()),startNode.get(children[i],UNKNOWN_ATTRIB));
				}
			}
		}catch(BackingStoreException be){
			be.printStackTrace();
			}
		

	}
	public void savePreferences(Preferences startNode) {
		for (Enumeration e = attributes.keys(); e.hasMoreElements();) {
			String keyVal = (String)e.nextElement();
			if(this.getAttribute(keyVal) instanceof String){
				startNode.put(SUFFIX_TAG+keyVal,(String)getAttribute(keyVal));
			}
		}
	}
}
