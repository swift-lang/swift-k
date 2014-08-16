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
 * Created on Sep 22, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public final class TimeInterval implements Comparable<TimeInterval> {
    private long ms;
    public static final TimeInterval FOREVER = new TimeInterval(Long.MAX_VALUE/2);
    public static final TimeInterval SECOND = new TimeInterval(1000);
    public static final TimeInterval MINUTE = SECOND.multiply(60);
    public static final TimeInterval HOUR = MINUTE.multiply(60);
    public static final TimeInterval DAY = HOUR.multiply(24);

    private TimeInterval(long ms) {
        this.ms = ms;
    }

    public long getSeconds() {
        return ms / 1000;
    }

    public TimeInterval add(TimeInterval s) {
        return new TimeInterval(ms + s.ms);
    }

    public TimeInterval multiply(int factor) {
        return new TimeInterval(ms * factor);
    }

    public TimeInterval subtract(TimeInterval s) {
        return new TimeInterval(ms - s.ms) ;
    }

    public TimeInterval divide(int d) {
        return new TimeInterval(ms / d);
    }
    
    private static final NumberFormat NF = new DecimalFormat("0.000s");
    
    public String toString() {
    	return NF.format((double) ms / 1000);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TimeInterval) {
        	return ms == ((TimeInterval) obj).ms;
        }
        else {
        	return false;
        }
    }

    public int hashCode() {
        return (int) ms;
    }

    public boolean isGreaterThan(TimeInterval s) {
        return ms > s.ms;
    }
    
    public boolean isLessThan(TimeInterval s) {
        return ms < s.ms;
    }
    
    public long getMilliseconds() {
        return ms;
    }
    
    public static TimeInterval fromSeconds(long seconds) {
        return new TimeInterval(seconds * 1000);
    }
    
    public static TimeInterval fromMilliseconds(long ms) {
        return new TimeInterval(ms);
    }
    
    public static TimeInterval min(TimeInterval a, TimeInterval b) {
        return a.ms < b.ms ? a : b;
    }

    public static TimeInterval max(TimeInterval a, TimeInterval b) {
        return a.ms > b.ms ? a : b;
    }

    public int compareTo(TimeInterval ti) {
        return sgn(ms - ti.ms);
    }
    
    private static int sgn(long v) {
        if (v == 0) {
            return 0;
        }
        else if (v < 0) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
