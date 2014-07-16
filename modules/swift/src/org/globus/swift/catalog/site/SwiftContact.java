//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.globus.swift.catalog.site;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.vdl.engine.Warnings;

public class SwiftContact extends BoundContact {
    private Map<String, Application> apps;
    private SwiftContactSet siteCatalog;
    
    public SwiftContact() {
        super();
    }

    public SwiftContact(String name) {
        super(name);
    }

    public void addApplication(Application app) {
        if (apps == null) {
            apps = new HashMap<String, Application>();
        }
        if (apps.put(app.getName(), app) != null) {
            Warnings.warn(Warnings.Type.SITE, "Multiple entries found for application '" + 
                app.getName() + "' on site '" + this.getName()  + "'");
        }
    }

    public Collection<Application> getApplications() {
        if (apps == null) {
            return Collections.emptyList();
        }
        else {
            return apps.values();
        }
    }

    public SwiftContactSet getSiteCatalog() {
        return siteCatalog;
    }

    public void setSiteCatalog(SwiftContactSet siteCatalog) {
        this.siteCatalog = siteCatalog;
    }
    
    /**
     * Get an application with the specified tr. Only this site
     * is searched. Returns <code>null</code> if not found.
     */
    public Application getApplication(String tr) {
        if (apps == null) {
            return null;
        }
        else {
            return apps.get(tr);
        }
    }

    /**
     * Find apps by searching in the following order:
     * <ol>
     *  <li>host:tr</li>
     *  <li>host:*</li>
     *  <li>pool:tr</li>
     *  <li>pool:*</li>
     * </ol>
     */
    public Application findApplication(String tr) {

        Application app = null;
        if (apps != null) {
            app = apps.get(tr);
            if (app == null) {
                app = apps.get("*");
            }
        }
        if (app == null) {
            app = siteCatalog.findApplication(tr);
        }
        return app;
    }
}
