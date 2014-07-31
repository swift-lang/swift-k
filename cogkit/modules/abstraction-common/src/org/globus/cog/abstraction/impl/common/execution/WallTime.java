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
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.impl.common.execution;

import java.util.HashMap;
import java.util.Map;

public class WallTime implements Comparable<WallTime> {
    private static final Map<String, Formatter> FORMATTERS;
    private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

    static {
        FORMATTERS = new HashMap<String, Formatter>();
        FORMATTERS.put(null, DEFAULT_FORMATTER);
        FORMATTERS.put("default", DEFAULT_FORMATTER);
        FORMATTERS.put("pbs", new PBSFormatter());
        FORMATTERS.put("hms", new HHMMSSFormatter());
        FORMATTERS.put("globus-jobmanager-pbs", new PBSFormatter());
        FORMATTERS.put("pbs-native", new NativePBSFormatter());
        FORMATTERS.put("sge-native", new NativeSGEFormatter());
    }

    public static Formatter getFormatter(String type) {
        if (type != null) {
            type = type.toLowerCase();
        }
        Formatter f = FORMATTERS.get(type);
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
    
    public boolean equals(Object obj) {
        if (obj instanceof WallTime) {
            WallTime wt = (WallTime) obj;
            return seconds == wt.seconds;
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return seconds;
    }

    public static String format(String type, long seconds) {
        return getFormatter(type).format(seconds);
    }

    public static String format(long seconds) {
        return format(null, seconds);
    }

    public static String normalize(String spec, String target) {
        return format(target, timeToSeconds(spec));
    }

    private static void pad(StringBuffer sb, long value) {
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
        System.out.println(normalize("10:00", "pbs-native"));
        System.out.println(normalize("1:10:01", "pbs-native"));
        System.out.println(normalize("10", "pbs-native"));
        System.out.println(normalize("2:35:50", "pbs-native"));
    }

    public static interface Formatter {
        String format(long seconds);
    }

    private static class DefaultFormatter implements Formatter {
        public String format(long seconds) {
            return String.valueOf((long) Math.ceil((double) seconds / 60));
        }
    }

    private static class HHMMSSFormatter implements Formatter {
    	
        private static long seconds(long secondsInterval) {
            return secondsInterval % 60;
        }

        private static long minutes(long secondsInterval) {
            return (secondsInterval / 60) % 60;
        }

        private static long hours(long secondsInterval) {
            return secondsInterval / 3600;
        }

        public String format(long seconds) {
            StringBuffer sb = new StringBuffer();
            pad(sb, hours(seconds));
            sb.append(':');
            pad(sb, minutes(seconds));
            sb.append(':');
            pad(sb, seconds(seconds));
            return sb.toString();
        }
    }

    private static class PBSFormatter extends DefaultFormatter {
    }

    /**
     * http://www.clusterresources.com/wiki/doku.php?id=torque:2.1_job_submission
     * 
     * Seconds or [[HH:]MM:]SS
     * 
     */
    private static class NativePBSFormatter extends HHMMSSFormatter {
    }
    
    private static class NativeSGEFormatter extends HHMMSSFormatter {
    }

    public int compareTo(WallTime o) {
        return seconds - o.seconds;
    }
}
