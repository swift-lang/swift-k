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
 * Created on Jul 1, 2014
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.griphyn.vdl.karajan.monitor.MonitorAppender;

public class SwiftLogInfo {
    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSZ");
    
    public static final int FOLLOW_SLEEP_TIME = 50;
    
    private String logFileName;
    private boolean follow;
    private double rate;
    private int port;
    private int timeout;
    private long lastActivityTimestamp;
    
    public SwiftLogInfo(int port, String logFileName, boolean follow, double rate, int timeout) {
        this.port = port;
        this.logFileName = logFileName;
        this.follow = follow;
        this.rate = rate;
        this.timeout = timeout;
    }
    
    public void run() throws Exception {
        updateTimestamp();
        MonitorAppender ap;
        if (port == -1) {
            ap = new MonitorAppender("bla", "http");
        }
        else {
            ap = new MonitorAppender("bla", "http:" + port);
        }
        BufferedReader br = new BufferedReader(new FileReader(logFileName));
        
        long filesz = new File(logFileName).length();
        long pos = 0;
        int lastTenth = 0;
        long firstLogTime = -1;
        long firstActualTime = System.currentTimeMillis();
        
        if (follow) {
            System.out.print("Following " + logFileName + ". Hit CTRL+C to end.");
        }
        else {
            System.out.print("Parsing " + logFileName + "...");
        }

        StringBuilder crt = new StringBuilder();
        String line = br.readLine();
        while (follow || (line != null)) {
            if (timedOut()) {
                System.out.println("timed out...");
                break;
            }
            if (line == null) {
                Thread.sleep(FOLLOW_SLEEP_TIME);
                line = br.readLine();
                continue;
            }
            if (isMessageHeader(line)) {
                firstLogTime = commit(crt, firstLogTime, firstActualTime, ap);
            }
            pos = pos + line.length() + 1;
            int tenth = (int) (pos * 10 / filesz);
            if (tenth != lastTenth) {
                System.out.print('.');
                lastTenth = tenth;
            }
            append(crt, line);
            updateTimestamp();
            line = br.readLine();
        }
        commit(crt, firstLogTime, firstActualTime, ap);
        System.out.println("done");
    }

    private boolean timedOut() {
        if (timeout > 0) {
            long now = System.currentTimeMillis();
            if ((now - lastActivityTimestamp) > timeout * 1000) {
                return true;
            }
        }
        return false;
    }

    private void updateTimestamp() {
        lastActivityTimestamp = System.currentTimeMillis();
    }

    private long commit(StringBuilder crt, long firstLogTime, long firstActualTime, MonitorAppender ap) 
            throws InterruptedException, ParseException {
        if (crt.length() == 0) {
            return firstLogTime;
        }
        String[] els = split(crt);
        long time = parseTime(els);
        
        if (rate != 0) {
            if (firstLogTime == -1) {
                firstLogTime = time;
            }
            long now = System.currentTimeMillis();
            // this event is supposed to happen at this relative time
            long deadline = (long) ((time - firstLogTime) / rate);
            long delay = deadline - (now - firstActualTime);
            if (delay >= 0) {
                Thread.sleep(delay);
            }
        }
                    
        LoggingEvent le = new LoggingEvent(els[2], Logger.getLogger(els[2]), time, getLevel(els[1]), els[3], null);
        ap.doAppend(le);
        
        return firstLogTime;
    }
    
    private static Calendar cal = new GregorianCalendar();
    private long lastTime = -1;
    private int lasthh, lastmm, lastss, lastms;
    // out of laziness, assume that there is at least one event per day

    private long parseTime(String[] els) throws ParseException {
        // if used, SimpleDateFormat.parse takes 60% of the CPU time 
        String ts = els[0];
        try {
            int yyyy = Integer.parseInt(ts.substring(0, 4));
            int MM = Integer.parseInt(ts.substring(5, 7));
            int dd = Integer.parseInt(ts.substring(8, 10));
            
            int hh = Integer.parseInt(ts.substring(11, 13));
            int mm = Integer.parseInt(ts.substring(14, 16));
            int ss = Integer.parseInt(ts.substring(17, 19));
            int ms = Integer.parseInt(ts.substring(20, 22));
            if (lastTime > 0) {
                if (lastss == ss && lastmm == mm && lasthh == hh) {
                    lastTime = lastTime + ms - lastms; 
                
                    lasthh = hh;
                    lastmm = mm;
                    lastss = ss;
                    lastms = ms;
                    return lastTime;
                }
            }
            lasthh = hh;
            lastmm = mm;
            lastss = ss;
            lastms = ms;
        
            // use local time
            // int tz = Integer.parseInt(ts.substring(22, 25));
            cal.set(yyyy, MM, dd, hh, mm, ss);
            lastTime = cal.getTimeInMillis() + ms;
            return lastTime;
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Could not parse timestamp '" + ts + "'", e);
        }
    }
    
    private final int TIMESTAMP_LEN = 28;
    private final int CATEGORY_START = 29;
    private final int CLASS_START = 35;

    private String[] split(StringBuilder crt) {
        String[] els = new String[5];
        els[0] = crt.substring(0, TIMESTAMP_LEN);
        for (int i = CATEGORY_START; i < crt.length(); i++) {
            if (crt.charAt(i) == ' ') {
                els[1] = crt.substring(CATEGORY_START, i);
                break;
            }
        }
        
        for (int i = CLASS_START; i < crt.length(); i++) {
            if (crt.charAt(i) == ' ') {
                els[2] = crt.substring(CLASS_START, i);
                els[3] = crt.substring(i + 1);
                break;
            }
        }
        
        crt.delete(0, crt.length());
        
        return els;
    }

    private void append(StringBuilder crt, String line) {
        if (crt.length() > 0) {
            crt.append('\n');
        }
        crt.append(line);
    }
    
    private static final String HEADER_FMT = "0000-00-00 00:00:00,000?0000";

    private boolean isMessageHeader(String line) {
        if (line.length() < HEADER_FMT.length()) {
            return false;
        }
        for (int i = 0; i < HEADER_FMT.length(); i++) {
            char expected = HEADER_FMT.charAt(i);
            char actual = line.charAt(i);
            switch (expected) {
                case '0':
                    if (actual < '0' || actual > '9') {
                        return false;
                    }
                    break;
                case '?':
                    break;
                default:
                    if (actual != expected) {
                        return false;
                    }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        ArgumentParser ap = new ArgumentParser();
        ap.addFlag("f", "Follow: keep parsing the log file as it grows.");
        ap.addOption("rt", "Real time: if specified, " +
        		"generate log events progressively at a rate " +
        		"proportional to that at which they were generated.", 
        		"integer", ArgumentParser.OPTIONAL);
        ap.addOption("p", "Port: specify the port on which to "
                + "start the http service. Use 0 (zero) to have "
                + "a port picked automatically.", "integer", ArgumentParser.OPTIONAL);
        ap.addOption("t", "Timeout: terminate after this many "
                + "seconds of inactivity", "seconds", ArgumentParser.OPTIONAL);
        ap.addOption(ArgumentParser.DEFAULT, "The log file to parse", 
            "logFile", ArgumentParser.NORMAL);
        ap.addFlag("h", "Display usage information");
        
        try {
            ap.parse(args);
            if (ap.isPresent("h")) {
                ap.usage();
                System.exit(0);
            }
            if (!ap.isPresent(ArgumentParser.DEFAULT)) {
                System.err.println("Missing log file name");
                ap.usage();
                System.exit(1);
            }
            SwiftLogInfo sli = new SwiftLogInfo(ap.getIntValue("p", -1), ap.getStringValue(ArgumentParser.DEFAULT), 
                ap.isPresent("f"), ap.getFloatValue("rt", 0), ap.getIntValue("t", -1));
            sli.run();
        }
        catch (ArgumentParserException e) {
            System.err.println(e.getMessage());
            ap.usage();
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    private static Priority getLevel(String s) {
        if ("WARN".equals(s)) {
            return Level.WARN;
        }
        else if ("ERROR".equals(s)) {
            return Level.ERROR;
        }
        else if ("INFO".equals(s)) {
            return Level.INFO;
        }
        else if ("DEBUG".equals(s)) {
            return Level.DEBUG;
        }
        else {
            return Level.ALL;
        }
    }
}
