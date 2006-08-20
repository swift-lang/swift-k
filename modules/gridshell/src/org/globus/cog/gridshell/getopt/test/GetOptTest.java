/*
 * Created on Jan 5, 2005
 */
package org.globus.cog.gridshell.getopt.test;

import org.globus.cog.gridshell.getopt.app.*;
import org.globus.cog.gridshell.getopt.interfaces.*;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;

import junit.framework.TestCase;

/**
 * JUnit test of org.globus.cog.gridface.impl.gridshell.getopt.app.GetOptImpl
 * 
 * 
 */
public class GetOptTest extends TestCase {
	private GetOpt getopt;

	private Scope scope;

	public void setUp() throws Exception {
		super.setUp();
		// my set up

		// scope
		scope = new ScopeImpl();
		scope.setVariableTo("var_1", "value_1");
		scope.setVariableTo("var_2", "value 2");
		scope.setVariableTo("var_3", "value_3");
		scope.setVariableTo("var_4", "value 4");
		scope.setVariableTo("var_5", "value 5");

		// getopt
		getopt = new GetOptImpl(scope);
	}

	public void tearDown() throws Exception {
		// my tear down
		getopt = null;
		scope = null;
		super.tearDown();
	}
	
	public void testOption() {
		Option option1 = new OptionImpl("option 1",java.lang.String.class,"a","option1");
		Option option2 = new OptionImpl("option 2",java.lang.String.class,"b","option2");
		
		// check if both null
		try {
			new OptionImpl(null,null,null,null);
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want an exception here
		}
		// check if length of short wrong
		try {
			new OptionImpl(null,null,"ab",null);
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want an exception here
		}
		// check if length of long wrong
		try {
			new OptionImpl(null,null,"a","b");
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want an exception here
		}
		// check if non word characters
		try {
			new OptionImpl(null,null,"#",null);
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want an exception here
		}
        // check if non word characters
		try {
			new OptionImpl(null,null,"a","a c");
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want an exception here
		}
	}	
	
	public void testArg() {
		Option optionOverrideArg = new OptionImpl(null,null,"a","bc");
		Argument arg1 = new ArgumentImpl(null,null);
		Argument arg2 = new ArgumentImpl(null,null,optionOverrideArg);
		
		assertFalse(arg1.isSet());
		arg1.setValue("newValue");
		assertTrue(arg1.isSet());
		
		assertFalse(arg2.isSet());
		optionOverrideArg.setValue("optionValue");
		assertTrue(arg2.isSet());
		arg2.setValue("argValue");
		
		optionOverrideArg.setValue(null);
		assertEquals("argValue",arg2.getValue());
		optionOverrideArg.setValue("optionValue");
		assertEquals(optionOverrideArg.getValue(),arg2.getValue());		
	}
	
	public void testDefaultValidators() throws Exception {
		// init some options
		Option bool = new OptionImpl("a boolean",java.lang.Boolean.class,"a","bc");
		Option doubl = new OptionImpl("a double",Double.class,"a","bc");
		Option integer = new OptionImpl("an integer",Integer.class,"a","bc");		
		Option string = new OptionImpl("a string",String.class,"a","bc");
		Option url = new OptionImpl("a double",java.net.URL.class,"a","bc");
		
		// all non-string options (strings don't get errors)
		Option[] nonStringOptions = {bool,doubl,integer,url};
		
		// Test values to fail
		for(int i=0;i<nonStringOptions.length;i++) {
			try {
				nonStringOptions[i].setValue("abc");
				fail("Error: Expected Exception");
			}catch(Exception successful) {
				// we want an exception here					
			}	
		}
		
		// Test instances and setable values
		String value = "true";
		
		bool.setValue(value);
		assertTrue(bool.getValue() instanceof Boolean);
		assertEquals(bool.getValue(),new Boolean(value));		
		
		value = "1.234";
		doubl.setValue(value);
		assertTrue(doubl.getValue() instanceof Double);
		assertEquals(doubl.getValue(),new Double(value));
		
		value = "123";
		integer.setValue(value);
		assertTrue(integer.getValue() instanceof Integer);
		assertEquals(integer.getValue(),new Integer(value));
		
		value = "abc";
		string.setValue(value);
		assertTrue(string.getValue() instanceof String);
		assertEquals(string.getValue(),value);
		
		value = "http://www.google.com:80/";
		url.setValue(value);
		assertTrue(url.getValue() instanceof java.net.URL);
		assertEquals(url.getValue(),new java.net.URL(value));		
	}
	
	public void testStrictAndTwice() {
		String commandLine = "command_name arg1 arg2";
		
		// testing strict
		try {
			getopt.isAllowDynamicArgs(false);
			getopt.parse(commandLine);			
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want to get here
		}
		
		// testing parse twice
		commandLine = "command_name";
		try {
			getopt.parse(commandLine);
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			// we want to get here
		}		
	}
	
	public void testParseArgument() {
		String[] args = { "command_name","arg1", "arg 2", "argument's 3 value"};
		String commandLine = args[0]+" "+args[1]+" '"+args[2]+"' \""+args[3]+"\"";		
		
		for(int i=0;i<args.length-1;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}
		getopt.parse(commandLine);
		
		for(int i=0;i<args.length;i++) {
			assertEquals(args[i],getopt.getArgumentAt(i).getValue());
		}
	}
	
	public void testOptions() {
		String commandLine = "'command name' -ac a_optvalue 'c\\'s value' --bb 'bb\\'s value'";
		Option[] options = { 
				new OptionImpl(null,null,"a","aa",false),
				new OptionImpl(null,null,"b","bb",false),				
				new OptionImpl(null,null,false,null,"c","cc",false)
		};
		getopt.addOptions(options);
		
		getopt.parse(commandLine);
				
		assertEquals("a_optvalue",getopt.getOption("aa").getValue());
		assertEquals("bb's value",getopt.getOption("bb").getValue());
		assertEquals("c's value",getopt.getOption("c").getValue());
	}
	
	public void testRequired() {
		Option[] options = { 
				new OptionImpl(null,null,"a","aa"),
				new OptionImpl(null,null,"b","bb"),				
				new OptionImpl(null,null,false,"c","cc")
		};
		getopt.addOptions(options);
		
		String[] args = { "command_name","arg1", "arg 2", "argument's 3 value"};
		String commandLine = args[0]+" -a 'option a\\'s value' "+args[1]+" '"+args[2]+"' \""+args[3]+"\"";		
		
		for(int i=0;i<args.length-1;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}
		
		try {
			getopt.parse(commandLine);
			getopt.checkRequired();
			fail("Error: Expected Exception");
		}catch (Exception successful) {
			String errorMessage = successful.getMessage();		
			assertTrue(errorMessage.indexOf("bb") != -1);
			assertTrue(errorMessage.indexOf("cc") == -1);
		}
	}
	
	public void testEnvVar() {
		Option[] options = { 
				new OptionImpl(null,null,"a","aa",false),
				new OptionImpl(null,null,"b","bb",false),				
				new OptionImpl(null,null,false,null,"c","cc",false)
		};
		getopt.addOptions(options);

		String commandLine = "'command name' ${var_1} -ab 'option a has ${var_4}' b 'arg 2' \"argument's 4 ${var_3}value\"";		
		
		for(int i=0;i<3;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}
		
		getopt.parse(commandLine);
		
		assertEquals("command name",getopt.getArgumentAt(0).getValue());
		assertEquals("option a has value 4",getopt.getOption("a").getValue());
		assertEquals("b",getopt.getOption("b").getValue());
		assertEquals("value_1",getopt.getArgumentAt(1).getValue());
		assertEquals("arg 2",getopt.getArgumentAt(2).getValue());
		assertEquals("argument's 4 value_3value",getopt.getArgumentAt(3).getValue());
		
	}
	
	public void testNullEnvVar() {
		getopt = new GetOptImpl(null);
		
		Option[] options = { 
				new OptionImpl(null,null,"a","aa",false),
				new OptionImpl(null,null,"b","bb",false),				
				new OptionImpl(null,null,false,null,"c","cc",false)
		};
		getopt.addOptions(options);

		String commandLine = "'command name' ${var_1} -ab 'option a has ${var_4}' b 'arg 2' \"argument's 4 ${var_3}value\"";		
		
		for(int i=0;i<3;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}
		try {
		  getopt.parse(commandLine);
		  fail("Expected exception due to undefined scope for variables");
		}catch(Exception success) {
			// we want to get here..we expect an exception
		}		
	}
	public void testEscapedVar() {
		Option[] options = { 
				new OptionImpl(null,null,"a","aa",false),
				new OptionImpl(null,null,"b","bb",false),				
				new OptionImpl(null,null,false,null,"c","cc",false)
		};
		getopt.addOptions(options);

		String commandLine = "'command name' ${var_1} -ab 'option a has \\${var_4}' b 'arg 2' \"argument's 4 ${var_3}value\"";		
		
		for(int i=0;i<3;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}		
		
		getopt.parse(commandLine);
		
		assertEquals("command name",getopt.getArgumentAt(0).getValue());
		assertEquals("option a has ${var_4}",getopt.getOption("a").getValue());
		assertEquals("b",getopt.getOption("b").getValue());
		assertEquals("value_1",getopt.getArgumentAt(1).getValue());
		assertEquals("arg 2",getopt.getArgumentAt(2).getValue());
		assertEquals("argument's 4 value_3value",getopt.getArgumentAt(3).getValue());
	}
	public void testUndefinedVar() {
		Option[] options = { 
				new OptionImpl(null,null,"a","aa",false),
				new OptionImpl(null,null,"b","bb",false),				
				new OptionImpl(null,null,false,null,"c","cc",false)
		};
		getopt.addOptions(options);

		String commandLine = "'command name' ${var_1} -ab 'option a has ${NOT_DEFINED}' b 'arg 2' \"argument's 4 ${var_3}value\"";		
		
		for(int i=0;i<3;i++) {
			getopt.addArgument(new ArgumentImpl(null,null));
		}		
		
		getopt.parse(commandLine);
		
		assertEquals("command name",getopt.getArgumentAt(0).getValue());
		assertEquals("option a has null",getopt.getOption("a").getValue());
		assertEquals("b",getopt.getOption("b").getValue());
		assertEquals("value_1",getopt.getArgumentAt(1).getValue());
		assertEquals("arg 2",getopt.getArgumentAt(2).getValue());
		assertEquals("argument's 4 value_3value",getopt.getArgumentAt(3).getValue());
	
	}
	
	/**
	 * Dynamic options must be a flag, this is used when needing to get a command name so that we can get the options
	 */
	public void testDynamicOptions() {
		String commandLine = "'command name' ${var_1} -ab 'option a has ${var_4}' b 'arg 2' \"argument's 4 ${var_3}value\"";		
		
		getopt.isAllowDynamicOptions(true);
		getopt.isAllowDynamicArgs(true);
		
		getopt.parse(commandLine);
		
		assertEquals("command name",getopt.getArgumentAt(0).getValue());
		assertEquals(Boolean.TRUE,getopt.getOption("a").getValue());
		assertEquals("value_1",getopt.getArgumentAt(1).getValue());
		assertEquals(Boolean.TRUE,getopt.getOption("b").getValue());
		assertEquals("option a has value 4",getopt.getArgumentAt(2).getValue());
		assertEquals("b",getopt.getArgumentAt(3).getValue());
		assertEquals("arg 2",getopt.getArgumentAt(4).getValue());
		assertEquals("argument's 4 value_3value",getopt.getArgumentAt(5).getValue());	
	}
	
	
	
	
}