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
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class Time {
    public static final Time NEVER = new Time(Long.MAX_VALUE - Integer.MAX_VALUE);
    
    private long ms;
    
    private Time(long ms) {
        this.ms = ms;
    }
    
    public static Time now() {
        return new Time(System.currentTimeMillis());
    }

    public TimeInterval subtract(Time time) {
        return TimeInterval.fromMilliseconds(ms - time.ms);
    }

    public Time add(TimeInterval ti) {
        return new Time(ms + ti.getMilliseconds());
    }

    public Time subtract(TimeInterval ti) {
        return new Time(ms - ti.getMilliseconds());
    }
    
    public static Time min(Time a, Time b) {
        return a.ms < b.ms ? a : b;
    }

    public static Time max(Time a, Time b) {
        return a.ms > b.ms ? a : b;
    }

    public boolean isGreaterThan(Time t) {
        return ms > t.ms;
    }
    
    public long getSeconds() {
        return ms / 1000;
    }
    
    public long getMilliseconds() {
        return ms;
    }
    
    public static Time fromSeconds(long seconds) {
        return new Time(seconds * 1000);
    }
    
    public static Time fromMilliseconds(long ms) {
        return new Time(ms);
    }
    
    public String toString() {
        return getSeconds() + "s";
    }
}
