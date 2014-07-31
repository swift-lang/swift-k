/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactSet {
	private Map<String, BoundContact> contacts;
	private List<BoundContact> cl;

	public ContactSet() {
		contacts = new HashMap<String, BoundContact>();
	}

	public void addContact(BoundContact contact) {
		contacts.put(contact.getName(), contact);
		if (cl == null) {
			cl = new ArrayList<BoundContact>();
		}
		cl.add(contact);
	}

	public void removeContact(BoundContact contact) {
		contacts.remove(contact.getName());
		cl.remove(contact);
	}

	public BoundContact getContact(String hostname) {
		return contacts.get(hostname);
	}

	public int size() {
		return contacts.size();
	}

	public BoundContact get(int index) {
		if (cl == null) {
			return null;
		}
		return cl.get(index);
	}

	public int indexOf(BoundContact contact){
		return cl.indexOf(contact);
	}

	public List<BoundContact> getContacts() {
		return cl;
	}

	public void setContacts(List<BoundContact> cl) {
		this.cl = cl;
		for (BoundContact contact : cl) {
			contacts.put(contact.getName(), contact);
		}
	}

	public String toString() {
		return contacts.toString();
	}
}
