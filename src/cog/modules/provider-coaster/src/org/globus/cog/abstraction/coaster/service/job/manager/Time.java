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
