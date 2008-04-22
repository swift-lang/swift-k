//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public class WallTime {
    private int seconds;
    private String spec;
    
    public WallTime(int seconds) {
        this.seconds = seconds;
        this.spec = secondsToTime(seconds); 
    }
    
    public WallTime(String spec) {
        this.spec = spec;
        this.seconds = timeToSeconds(spec); 
    }
    
    public String getSpec() {
        return spec;
    }
    
    public int getSeconds() {
        return seconds;
    }
    
    public String toString() {
        return spec;
    }
    
    public static String secondsToTime(int seconds) {
        StringBuffer sb = new StringBuffer();
        pad(sb, seconds / 3600);
        sb.append(':');
        pad(sb, (seconds % 3600) / 60);
        sb.append(':');
        pad(sb, seconds % 60);
        return sb.toString();
    }

    private static void pad(StringBuffer sb, int value) {
        if (value < 10) {
            sb.append('0');
        }
        sb.append(String.valueOf(value));
    }

    /**
     * Valid times formats: Minutes, Hours:Minutes, Hours:Minutes:Seconds
     */
    public static int timeToSeconds(String time) {
        String[] s = time.split(":");
        try {
            if (s.length == 1) {
                return 60 * Integer.parseInt(s[0]);
            }
            else if (s.length == 2) {
                return 60 * Integer.parseInt(s[1]) + 3600 * Integer.parseInt(s[0]);
            }
            else if (s.length == 3) {
                return Integer.parseInt(s[2]) + 60 * Integer.parseInt(s[1]) + 3600
                        * Integer.parseInt(s[0]);
            }
        }
        catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException("Invalid time specification: " + time);
    }
}
