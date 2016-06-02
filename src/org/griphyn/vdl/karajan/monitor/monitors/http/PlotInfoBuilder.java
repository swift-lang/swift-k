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
 * Created on Jul 21, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.globus.cog.util.json.JSONEncoder;
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
    public ByteBuffer getData(Map<String, String> params) throws IOException {
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