//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 29, 2014
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;

public class AppsSummaryBuilder {

    private final SortedMap<String, List<SortedSet<ApplicationItem>>> byName;
    private BrowserDataBuilder db;

    public AppsSummaryBuilder(BrowserDataBuilder db) {
        this.byName = db.getByName();
        this.db = db;
    }

    public void getData(JSONEncoder e) {
        // counts of each state by name
        e.beginMap();
          db.writeEnabledStates(e, "enabledStates");
          e.writeMapKey("apps");
          e.beginMap();
          for (Map.Entry<String, List<SortedSet<ApplicationItem>>> en : byName.entrySet()) {
              e.writeMapKey(en.getKey());
              e.beginArray();
                for (ApplicationState s : ApplicationState.values()) {
                    if (s.isEnabled()) {
                        e.beginArrayItem();
                          e.beginArray();
                            e.writeArrayItem(s.ordinal());
                            e.writeArrayItem(en.getValue().get(s.ordinal()).size());
                          e.endArray();
                        e.endArrayItem();
                    }
                }
              e.endArray();
          }
          e.endMap();
        e.endMap();
    }

}
