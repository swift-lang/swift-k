/*
 * 
 */
package org.globus.cog.gridshell.commands.test;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.GridShellProperties;
import org.globus.cog.gridshell.commands.CommandProperties;
import org.globus.cog.gridshell.interfaces.Scope;

import junit.framework.TestCase;

/**
 * 
 */
public class CommandPropertiesTest extends TestCase {
	private static final Logger logger = Logger
			.getLogger(CommandPropertiesTest.class);

	private static final String FILE_NAME = GridShellProperties.getDefault().getProperty("gridshell.test.basepath")
			+"/commands/CommandPropertiesTest/command.properties";

	protected CommandProperties cmdProperties;

	public void setUp() throws Exception {
		super.setUp();
		cmdProperties = new CommandProperties(new File(FILE_NAME));
	}

	public void tearDown() throws Exception {
		cmdProperties = null;
		super.tearDown();
	}

	/**
	 * Ensures that the correct command name is matched to the correct value
	 * based upon the super classes
	 */
	public void testInherit() {
		logger.info("testNameValues()");
		Scope cmds = cmdProperties.getCommands();
		Iterator iCmdNames = cmds.getVariableNames().iterator();
		while (iCmdNames.hasNext()) {
			String cmdName = (String) iCmdNames.next();
			String value = (String) cmds.getValue(cmdName);
			logger.debug("cmdName=" + cmdName + " value=" + value);
			String cmdNamePref = cmdName.substring(0, cmdName.indexOf(".") - 1);
			String cmdValuePref = value.substring(0, cmdName.indexOf(".") - 1);
			assertEquals(cmdNamePref, cmdValuePref);
		}
	}
	
	public void testCommandCount() throws Exception {
		logger.info("testCommandCount()");
		assertEquals(21,cmdProperties.getCommands().getVariableNames().size());
	}
	
	public void testEscapedCommandName() throws Exception {
		logger.info("testEscapedCommandName()");
		assertTrue(cmdProperties.getCommands().getVariableNames().contains("command. test\"here\""));
	}
}