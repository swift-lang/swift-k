package org.globus.cog.monitor.guss;
import java.util.Calendar;
/*A collection of methods for routine maintenance of the usage-stats
  database, runnable from the command line or as part of a script.*/
public class DatabaseTool {
    
    public static void main(String[] args) {
	try {
	    GUSSImpl guss = new GUSSImpl();
	    guss.preSummarizeDatabase(args[0]);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
