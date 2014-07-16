//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 8, 2014
 */
package org.griphyn.vdl.util;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.StringUtil;

public class SwiftConfigInfo {
    public static void main(String[] args) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("swift-config-info");
        ap.addFlag("help", "Prints usage information");
        ap.addFlag("list", "Lists all available configuration properties");
        ap.addOption(ArgumentParser.DEFAULT, "A property to print information about", 
            "property", ArgumentParser.OPTIONAL);
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
                System.exit(0);
            }
            if (ap.isPresent("list")) {
                list();
            }
            if (ap.hasValue(ArgumentParser.DEFAULT)) {
                info(ap.getStringValue(ArgumentParser.DEFAULT));
            }
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Error parsing command line: " + e.getMessage());
            ap.usage();
            System.exit(1);
        }
    }

    private static void info(String prop) {
        SwiftConfigSchema.Info info = SwiftConfig.SCHEMA.getPropertyDescriptions().get(prop);
        if (info != null) {
            if (info.doc == null) {
                System.out.println(prop + ": no description available");
            }
            else {
                System.out.println(prop + ":");
                System.out.println(StringUtil.wordWrap(info.doc, 6, 65));
            }
        }
        else {
            System.out.println(prop + ": unknown property");
        }
    }

    private static void list() {
        Collection<String> names = SwiftConfig.SCHEMA.listProperties();
        SortedSet<String> snames = new TreeSet<String>(names);
        for (String name : snames) {
            System.out.println(name.replace("\"*\"", "*"));
        }
    }
}
