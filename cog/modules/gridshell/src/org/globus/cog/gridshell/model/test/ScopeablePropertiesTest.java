/*
 * 
 */
package org.globus.cog.gridshell.model.test;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.model.ScopeableProperties;
import org.globus.cog.gridshell.test.AbstractGridshellTest;

/**
 * 
 */
public class ScopeablePropertiesTest extends AbstractGridshellTest {
    private static final Logger logger = Logger.getLogger(ScopeablePropertiesTest.class);
    
    public void testNoSuperSize() throws Exception {
        File file = this.getTestFile("test.properties");
        ScopeableProperties test = new ScopeableProperties(file);
        assertEquals(11,test.keySet().size());
    }    
    public void testValues() throws Exception {
        File file = this.getTestFile("test.properties");
        ScopeableProperties test = new ScopeableProperties(file);
        assertEquals("property value 2",test.getProperty("gridshell.property.2"));
        assertEquals("property value 1",test.getProperty("gridshell.property.1"));
        assertEquals(System.getProperty("java.home"),test.getProperty("java.home"));
        assertEquals(System.getProperty("user.home"),test.getProperty("user.home"));
    }
    public void testSuperSize() throws Exception {
        File file = this.getTestFile("test.properties");
        ScopeableProperties test = new ScopeableProperties(file,"inherit.file.");
        
        logger.debug("KEYS: "+test.keySet());
        assertEquals(19,test.keySet().size());
    }
    public void testSuperValues() throws Exception {
        File file = this.getTestFile("test.properties");
        ScopeableProperties test = new ScopeableProperties(file,"inherit.file.");
        Iterator iKeys = test.keySet().iterator();
        while(iKeys.hasNext()) {
            String key = (String)iKeys.next();
            String value = test.getProperty(key);
            logger.debug(key+"="+value);
            assertTrue(!"INVALID".equals(value));
        }
    }
    public void testKeySet() throws Exception {
        File file = this.getTestFile("test.properties");
        ScopeableProperties test = new ScopeableProperties(file,"inherit.file.");
        assertEquals(4,test.keySet("gridshell.property.").size());
        assertEquals(6,test.keySet("gridshell.").size());
    }
}
