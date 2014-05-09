//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.globus.swift.catalog.site;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.vdl.util.FQN;

public class SwiftContact extends BoundContact {
    private Map<FQN, Object> profiles = new HashMap<FQN, Object>();

    public SwiftContact() {
        super();
    }

    public SwiftContact(String host) {
        super(host);
    }

    public void addProfile(FQN fqn, String value) {
    	profiles.put(fqn, value);
    }

    public Map<FQN, Object> getProfiles() {
        return profiles;
    }
}
