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

import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Digester;

public class PackageList {

    private File dir;

    public PackageList(String dir) {
        this.dir = new File(dir);
    }

    public void generate() {
        if (!dir.exists()) {
            throw new RuntimeException(dir + " does not exist");
        }
        add("cog-abstraction-common-*.jar");
        add("cog-jglobus-*.jar");
        add("cog-karajan-*.jar");
        add("cog-provider-coaster-*.jar");
        add("cog-provider-gt2-*.jar");
        addOptional("cog-provider-gt4*.jar");
        add("cog-provider-local-*.jar");
        add("cog-provider-localscheduler-*.jar");
        addOptional("cog-provider-ssh-*.jar");
        add("cog-util-*.jar");
        add("cryptix*.jar");
        addOptional("concurrent*.jar");
        addOptional("j2ssh*.jar");
        addOptional("jaxrpc.jar");
        add("jce-*.jar");
        add("jgss.jar");
        add("log4j*.jar");
        addOptional("puretls.jar");
        addOptional("addressing*.jar");
        addOptional("commonj*.jar");
        addOptional("axis*.jar");
        addOptional("cog-axis*.jar");
        addOptional("globus_delegation*.jar");
        addOptional("globus_wsrf*.jar");
        addOptional("gram*.jar");
        addOptional("naming*.jar");
        addOptional("saaj*.jar");
        addOptional("wsdl4j*.jar");
        addOptional("wss4j*.jar");
        addOptional("commons-collections*.jar");
        addOptional("commons-digester*.jar");
        addOptional("commons-discovery*.jar");
        addOptional("commons-beanutils*.jar");
        add("commons-logging-*.jar");
        addOptional("wsrf*.jar");
        addOptional("xalan*.jar");
        addOptional("xercesImpl*.jar");
        addOptional("xml-apis*.jar");
        addOptional("xmlsec*.jar");
    }
    
    private void add(String name) {
        add(name, false);
    }
    
    private void addOptional(String name) {
        add(name, true);
    }

    private void add(String name, boolean optional) {
        final String filt = name.replaceAll("\\.", "\\.").replaceAll("\\*", ".*");
        File[] f = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(filt);
            }
        });
        if (f.length == 0) {
            if (optional) {
                return;
            }
            else {
                throw new RuntimeException("Missing package: " + name);
            }
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
