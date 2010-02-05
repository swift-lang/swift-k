package org.globus.swift.data.policy;

import java.util.List;

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
    
    public void action(String srcfile, String srcdir) {
        if (! Director.broadcasted(srcfile, srcdir))
            callScript(srcfile, srcdir, destination);
    }
    
    void callScript(String srcfile, String srcdir, String destination) { 
        String home = System.getProperties().getProperty("swift.home");
        try {
            String[] line = new String[4];
            line[0] = home+"/libexec/cdm_broadcast.sh";
            line[1] = srcfile; 
            line[2] = srcdir; 
            line[3] = destination;
            Process process = Runtime.getRuntime().exec(line);
            process.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not launch external broadcast");
        }
    }   
    
    public String getDestination() {
        return destination;
    }
    
    public String toString() {
        return "BROADCAST";
    }
}
