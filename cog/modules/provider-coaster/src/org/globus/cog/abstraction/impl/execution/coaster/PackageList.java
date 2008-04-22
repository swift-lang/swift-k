//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 19, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.File;
import java.io.FilenameFilter;

public class PackageList {

    private File dir;

    public PackageList(String dir) {
        this.dir = new File(dir);
    }

    public void generate() {
        if (!dir.exists()) {
            throw new RuntimeException(dir + " does not exist");
        }
        add("backport-util-concurrent.jar");
        add("cog-abstraction-common-*.jar");
        add("cog-jglobus-*.jar");
        add("cog-karajan-*.jar");
        add("cog-provider-coaster-*.jar");
        add("cog-provider-gt2-*.jar");
        add("cog-provider-gt4*.jar");
        add("cog-provider-local-*.jar");
        add("cog-provider-localscheduler-*.jar");
        add("cog-provider-ssh-*.jar");
        add("cog-util-*.jar");
        add("commons-logging-*.jar");
        add("cryptix*.jar");
        add("j2ssh*.jar");
        add("jaxrpc.jar");
        add("jce-*.jar");
        add("jgss.jar");
        add("log4j*.jar");
        add("puretls.jar");
    }

    private void add(String name) {
        final String filt = name.replaceAll("\\.", "\\.").replaceAll("\\*", ".*");
        File[] f = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(filt);
            }
        });
        if (f.length == 0) {
            throw new RuntimeException("Missing package: " + name);
        }
        else {
            for (int i = 0; i < f.length; i++) {
                checksum(f[i]);
            }
        }
    }

    private void checksum(File f) {
        try {
            System.out.println(f.getName() + " " + Digester.computeMD5(f));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum for "
                    + f + ": " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            error("Missing lib directory argument");
        }
        try {
            new PackageList(args[0]).generate();
        }
        catch (Exception e) {
            error(e.getMessage());
        }
    }

    private static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
