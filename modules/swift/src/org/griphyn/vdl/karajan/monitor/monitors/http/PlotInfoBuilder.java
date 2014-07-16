//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.common.DataSampler.Series;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class PlotInfoBuilder extends StateDataBuilder {
    
    private final SystemState state;
    private int maxCount;
    private JSONEncoder e;

    PlotInfoBuilder(SystemState state) {
        this.state = state;
    }

    @Override
    public ByteBuffer getData(Map<String, String> params) {
        DataSampler sampler = (DataSampler) state.getItemByID(DataSampler.ID, StatefulItemClass.WORKFLOW);
        
        Map<String, List<Series<?>>> series = sampler.getCategories();
        
        e = new JSONEncoder();
        e.beginArray();
        for (Map.Entry<String, List<Series<?>>> e1 : series.entrySet()) {
          e.beginArrayItem();
            e.beginMap();
              
              e.writeMapItem("category", e1.getKey());
              e.writeMapKey("series");
                e.beginArray();
                  for (Series<?> s : e1.getValue()) {
                    e.beginArrayItem();
                    e.beginMap();
                      e.writeMapItem("key", s.getKey());
                      e.writeMapItem("label", s.getLabel());
                      e.writeMapItem("unit", s.getUnit().getName());
                      e.writeMapItem("unitType", s.getUnit().getType());
                    e.endMap();
                    e.endArrayItem();
                  }
                e.endArray();
                
              e.endMap();
            e.endArrayItem();
        }
        e.endArray();
        
        return ByteBuffer.wrap(e.toString().getBytes());
    }
}