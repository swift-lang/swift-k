
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class ContactSet {
	public Map contacts;
	public List cl;

	public ContactSet() {
		contacts = new HashMap();
	}

	public void addContact(BoundContact contact) {
		contacts.put(contact.getHost(), contact);
		if (cl == null) {
			cl = new ArrayList();
		}
		cl.add(contact);
	}

	public void removeContact(BoundContact contact) {
		contacts.remove(contact.getHost());
		cl.remove(contact);
	}

	public BoundContact getContact(String ip) {
		return (BoundContact) contacts.get(ip);
	}

	public int size() {
		return contacts.size();
	}

	public BoundContact get(int index) {
		if (cl == null) {
			return null;
		}
		return (BoundContact) cl.get(index);
	}
	
	public int indexOf(BoundContact contact){
		return cl.indexOf(contact);
	}
	
	public List getContacts() {
		return cl;
	} 
	
	public void setContacts(List cl) {
		this.cl = cl;
		for (Iterator i = cl.iterator(); i.hasNext();) {
			BoundContact contact = (BoundContact) i.next();
			contacts.put(contact.getHost(), contact);
		}
	}
}
