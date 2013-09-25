/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.globus.swift.data.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.swift.data.Director;

public class Broadcast extends Policy {
    
    static Logger logger = Logger.getLogger(Broadcast.class);
    
    String destination = null; 
    
    @Override
    public void settings(List<String> settings) {
        try {
            destination = settings.get(0);
        } 
        catch (Exception e) { 
            throw new RuntimeException("Incorrect settings for BROADCAST");
        }
    }

    /**
       Call the external script to perform the broadcast for this batch.
    */
    public static void perform(Map<String,List<String>> batch) {
        String[] line = commandLine(batch);
        logger.debug("arguments: " + Arrays.toString(line));
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(line);
            process.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException
            ("Could not launch external broadcast", e);
        }
        int code = process.exitValue();
        if (code != 0)
            throw new RuntimeException("External broadcast failed!");
    }

    /**
       Generate the command line for the external broadcast script.
    */
    static String[] commandLine(Map<String,List<String>> batch) {
        String home = System.getProperties().getProperty("swift.home");
        List<String> line = new ArrayList<String>();
        line.add(home+"/libexec/cdm_broadcast.sh");
        line.add(Director.broadcastMode);
        if (logger.isDebugEnabled())
            line.add(Director.logfile);
        else 
            line.add("/dev/null");
        for (Map.Entry<String,List<String>> entry : batch.entrySet()) {
            line.add("-l");
            String location = entry.getKey();
            List<String> files = entry.getValue();
            line.add(location);
            for (String file : files) {
                line.add(file);
                line.add(getDestination(file));
            }
        }
        String[] result = new String[line.size()];
        line.toArray(result);
        return result;
    }

    /**
       Return the remote destination directory for this policy.
    */
    public String getDestination() {
        return destination;
    }

    /**
       Return the remote destination directory for this broadcasted file.
    */
    public static String getDestination(String file) {
        String result = null;
        Policy policy = Director.lookup(file);
        Broadcast broadcast = (Broadcast) policy;
        result = broadcast.getDestination();
        return result;
    }
    
    public String toString() {
        return "BROADCAST";
    }
}
