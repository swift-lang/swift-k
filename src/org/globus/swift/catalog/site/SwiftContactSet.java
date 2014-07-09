//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 22, 2014
 */
package org.globus.swift.catalog.site;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.griphyn.vdl.engine.Warnings;

public class SwiftContactSet extends ContactSet {
    private Map<String, Application> apps;

    public void addApplication(Application app) {
        if (apps == null) {
            apps = new HashMap<String, Application>();
        }
        if (apps.put(app.getName(), app) != null) {
            Warnings.warn(Warnings.Type.SITE, "Multiple entries found for application '" + 
                app.getName() + "' on site pool");
        }
    }

    @Override
    public void addContact(BoundContact contact) {
        throw new UnsupportedOperationException();
    }

    public void addContact(SwiftContact contact) {
        super.addContact(contact);
        contact.setSiteCatalog(this);
    }

    public Application findApplication(String tr) {
        Application app = null;
        if (apps != null) {
            app = apps.get(tr);
            if (app == null) {
                app = apps.get("*");
            }
        }
        return app;
    }

    public Map<String, Application> getApplications() {
        if (apps == null) {
            apps = new HashMap<String, Application>();
        }
        return apps;
    }
}
