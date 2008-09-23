//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;


public final class Seconds {
    private long seconds;
    public static final Seconds NEVER = new Seconds(Long.MAX_VALUE);

    public Seconds(long seconds) {
        this.seconds = seconds;
    }

    public static Seconds now() {
        return new Seconds(System.currentTimeMillis() / 1000);
    }

    public long getSeconds() {
        return seconds;
    }

    public Seconds add(Seconds s) {
        return new Seconds(seconds + s.seconds);
    }

    public Seconds multiply(int factor) {
        return new Seconds(seconds * factor);
    }

    public Seconds subtract(Seconds s) {
        return new Seconds(seconds - s.seconds) ;
    }

    public Seconds divide(int d) {
        return new Seconds(seconds / d);
    }
    
    public long toMilliseconds() {
    	return seconds * 1000;
    }
    
    public String toString() {
    	return seconds + "s";
    }

    public boolean equals(Object obj) {
        if (obj instanceof Seconds) {
        	return seconds == ((Seconds) obj).seconds;
        }
        else {
        	return false;
        }
    }

    public int hashCode() {
        return (int) seconds;
    }
}
