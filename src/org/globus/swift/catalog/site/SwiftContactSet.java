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
