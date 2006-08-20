/*
 * 
 */
package org.globus.cog.gridshell.test;

import java.io.File;

import org.globus.cog.gridshell.GridShellProperties;

import junit.framework.TestCase;

/**
 * 
 */
public class AbstractGridshellTest extends TestCase {
    public static final String PROP_TEST_DIR = "gridshell.test.basepath";
    public static final String TEST_DIR = GridShellProperties.getDefault().getProperty(PROP_TEST_DIR);
    
    public File getTestFile(String file) {
        File result = new File(TEST_DIR,classToPath(this.getClass()));
        result = new File(result.getAbsolutePath(),file);
        return result;
    }
    
    public static String classToPath(Class _class) {
        String className = _class.getName();
        StringBuffer result = new StringBuffer();
        for(int i=0;i<className.length();i++) {
            String c = String.valueOf(className.charAt(i));
            if(".".equals(c)) {
                c = File.separator;
            }
            result.append(c);
        }
        return result.toString();
    }
}
