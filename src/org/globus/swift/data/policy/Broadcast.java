package org.globus.swift.data.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.globus.swift.data.Director;

public class Broadcast extends Policy {
    
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
        System.out.println("Broadcast.perform(): " + Arrays.toString(line));
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(line);
            process.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not launch external broadcast");
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
        for (Map.Entry<String,List<String>> entry : batch.entrySet()) {
            line.add("-l");
            String location = entry.getKey();
            List<String> files = entry.getValue();
            line.add(location);
            for (String file : files) {
                line.add(file);
                line.add(getDestination(file)+"/"+file);
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
