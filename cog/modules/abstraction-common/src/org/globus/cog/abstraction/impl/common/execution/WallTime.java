//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.impl.common.execution;

import java.util.HashMap;
import java.util.Map;

public class WallTime {
    private static final Map FORMATTERS;
    private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

    static {
        FORMATTERS = new HashMap();
        FORMATTERS.put(null, DEFAULT_FORMATTER);
        FORMATTERS.put("default", DEFAULT_FORMATTER);
        FORMATTERS.put("pbs", new PBSFormatter());
    }

    public static Formatter getFormatter(String type) {
        if (type != null) {
            type = type.toLowerCase();
        }
        Formatter f = (Formatter) FORMATTERS.get(type);
        return f == null ? DEFAULT_FORMATTER : f;
    }

    private int seconds;

    public WallTime(int seconds) {
        this.seconds = seconds;
    }

    public WallTime(String spec) {
        this.seconds = timeToSeconds(spec);
    }

    public int getSeconds() {
        return seconds;
    }

    public String toString() {
        return String.valueOf(seconds);
    }

    public String format(String type) {
        return getFormatter(type).format(seconds);
    }

    public String format() {
        return format(null);
    }

    public static String format(String type, int seconds) {
        return getFormatter(type).format(seconds);
    }

    public static String format(int seconds) {
        return format(null, seconds);
    }

    public static String normalize(String spec, String target) {
        return format(target, timeToSeconds(spec));
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
                return 60 * Integer.parseInt(s[1]) + 3600
                        * Integer.parseInt(s[0]);
            }
            else if (s.length == 3) {
                return Integer.parseInt(s[2]) + 60 * Integer.parseInt(s[1])
                        + 3600 * Integer.parseInt(s[0]);
            }
        }
        catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException("Invalid time specification: "
                + time);
    }

    public static void main(String[] args) {
        System.out.println(format(timeToSeconds("10:00")));
        System.out.println(format(timeToSeconds("1:10:01")));
        System.out.println(format(timeToSeconds("10")));
    }

    public static interface Formatter {
        String format(int seconds);
    }

    private static class DefaultFormatter implements Formatter {
        public String format(int seconds) {
            return String.valueOf((int) Math.ceil((double) seconds / 60));
        }
    }

    private static class PBSFormatter extends DefaultFormatter {
    }
}
