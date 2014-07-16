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
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    
    public SwiftLogInfo(String logFileName, boolean follow, double rate) {
        this.logFileName = logFileName;
        this.follow = follow;
        this.rate = rate;
    }
    
    public void run() throws Exception {
        MonitorAppender ap = new MonitorAppender("bla", "http");
        BufferedReader br = new BufferedReader(new FileReader(logFileName));
        
        long firstLogTime = -1;
        long firstActualTime = System.currentTimeMillis();
        
        if (follow) {
            System.out.print("Following " + logFileName + ". Hit CTRL+C to end.");
        }
        else {
            System.out.print("Parsing " + logFileName + "...");
        }
        String line = null;
        while (follow || (line = br.readLine()) != null) {
            if (line == null) {
                Thread.sleep(FOLLOW_SLEEP_TIME);
                continue;
            }
            String[] els = line.split("\\s+", 5);
            if (els.length < 5) {
                continue;
            }
            
            long time;
            try {
                time = SDF.parse(els[0] + " " + els[1]).getTime();
            }
            catch (ParseException e) {
                continue;
            }
            
            if (rate != 0) {
                if (firstLogTime == -1) {
                    firstLogTime = time;
                }
                long now = System.currentTimeMillis();
                // this event is supposed to happen at this relative time
                long deadline = (long) ((time - firstLogTime) / rate);
                long delay = deadline - (now - firstActualTime);
                System.out.println("deadline: " + deadline + ", now: " + (now - firstActualTime));
                if (delay >= 0) {
                    Thread.sleep(delay);
                }
            }
                        
            LoggingEvent le = new LoggingEvent(els[3], Logger.getLogger(els[3]), time, getLevel(els[2]), els[4], null);
            ap.doAppend(le);
        }
        System.out.println("done");
    }

    public static void main(String[] args) {
        ArgumentParser ap = new ArgumentParser();
        ap.addFlag("f", "Follow: keep parsing the log file as it grows.");
        ap.addOption("rt", "integer", "Real time: if specified, " +
        		"generate log events progressively at a rate " +
        		"proportional to that at which they were generated.", 
        		ArgumentParser.OPTIONAL);
        ap.addOption(ArgumentParser.DEFAULT, "logFile", "The log file to parse", 
            ArgumentParser.NORMAL);
        ap.addFlag("h", "Display usage information");
        
        try {
            ap.parse(args);
            if (ap.isPresent("h")) {
                ap.usage();
                System.exit(0);
            }
            SwiftLogInfo sli = new SwiftLogInfo(ap.getStringValue(ArgumentParser.DEFAULT), 
                ap.isPresent("f"), ap.getFloatValue("rt", 0));
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
