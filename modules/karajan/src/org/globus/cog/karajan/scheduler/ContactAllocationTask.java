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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 3, 2006
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;

public class ContactAllocationTask extends TaskImpl {
	private BoundContact contact;
	private Contact virtualContact;
	
	public ContactAllocationTask() {
		setName("Contact allocation task");
	}

	@Override
	protected Identity newIdentity() {
		return null;
	}

	public BoundContact getContact() {
		return contact;
	}

	public void setContact(BoundContact contact) {
		this.contact = contact;
	}

	public int getRequiredServices() {
		return 1;
	}

	public void setVirtualContact(Contact vc) {
		this.virtualContact = vc;
	}
	
	public Contact getVirtualContact() {
		return this.virtualContact;
	}
}
