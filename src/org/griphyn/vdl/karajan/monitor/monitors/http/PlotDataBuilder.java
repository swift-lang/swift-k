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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.common.DataSampler.Series;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class PlotDataBuilder extends StateDataBuilder {
    
    private final SystemState state;
    private int maxCount;
    private JSONEncoder e;

    PlotDataBuilder(SystemState state) {
        this.state = state;
    }

    @Override
    public ByteBuffer getData(Map<String, String> params) {
        DataSampler sampler = (DataSampler) state.getItemByID(DataSampler.ID, StatefulItemClass.WORKFLOW);
        String key = params.get("name");
        long start = Long.parseLong(params.get("start")) / 1000;//ms
        long samplerOffset = sampler.getOffset();//s
        if (start < samplerOffset) {
            start = samplerOffset;
        }
        int index = (int) (start - samplerOffset);
        
        Series<?> s = sampler.getSeries(key);
        
        if (s == null) {
            throw new IllegalArgumentException("Invalid series: " + key);
        }
        else {
            e = new JSONEncoder();
        
            e.beginArray();
            ArrayList<?> l = s.getData(); 
            for (int i = index; i < l.size(); i++) {
                e.beginArrayItem();
                e.beginArray();
                e.writeArrayItem((i + samplerOffset) * 1000);
                e.writeArrayItem(l.get(i));
                e.endArray();
                e.endArrayItem();
            }
            e.endArray();
            return ByteBuffer.wrap(e.toString().getBytes());
        }
    }
}