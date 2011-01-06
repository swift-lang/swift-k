//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.persistent;

import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;


public class FileResourceImpl extends org.globus.cog.abstraction.impl.file.coaster.FileResourceImpl {

    public FileResourceImpl() {
        super(false);
    }

    public FileResourceImpl(String name, String protocol, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, protocol, serviceContact, securityContext, false);
    }
}
