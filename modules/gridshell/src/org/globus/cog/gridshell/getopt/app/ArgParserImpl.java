/*
 * 
 */
package org.globus.cog.gridshell.getopt.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.getopt.interfaces.ArgParser;
import org.globus.cog.gridshell.getopt.interfaces.Argument;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.getopt.interfaces.Option;
import org.globus.cog.gridshell.getopt.interfaces.Storable;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;

/**
 * An Implementation of ArgParser which parsers based on a Decission Tree
 * 
 */
public class ArgParserImpl implements ArgParser {
	private static Logger logger = Logger.getLogger(ArgParserImpl.class);
	
	// Each Env Variable is $+ENV_PREFIX+SOME_TEXT+ENV_SUFFIX
	private static final String ENV_PREFIX = "{", ENV_SUFFIX = "}";
	
	// The character used to escape values
	private static final String ESCAPE_CHAR="\\";
	
	// this is a mapping of triggers, the default is always '*'
	// triggers are what start and end a value and are mapped startSignal=>endSignal
	private Map signals = new HashMap();
	
	// the GetOpt associated with this ArgParser
	private GetOpt getOpt;
	
	// our index that we are currently parsing
	private int index = 0;
	
	// the commandLine value
	private String commandLine;
	
	// A listing of options found that need values still
	private List nonFlagOptions = new LinkedList(); 
	
	// A listing of the arguments from getOpt
	List arguments;
	
	/**
	 * Our constructor which accepts an instance of GetOpt
	 * 
	 * @param getOpt
	 */
	public ArgParserImpl() {		
		// add signals
		signals.put("*"," "); // the default trigger
		signals.put("'","'");
		signals.put("\"","\"");		
	}
	
	private GetOpt getGetOpt() {
		return this.getOpt;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.ArgParser#parse(java.lang.String)
	 */
	public synchronized void parse(GetOpt getOpt, String commandLine) {
		logger.debug("parsing= {{{"+commandLine+"}}}");
		// ensure member variables are correct
		this.getOpt = getOpt;
		this.commandLine = commandLine;
		this.index = 0;
		arguments = this.getOpt.getArguments();
		
		// start at the root of the tree
		root();
	}
	/**
	 * Processes variables in scope for value using this ArgParserImpl
	 * @param value
	 * @param scope
	 * @return
	 */
	public static String processVariablesForScope(String value, Scope scope) {
		if(logger.isDebugEnabled()) {
			logger.debug("processVariablesForScope("+value+","+scope+")");
		}
		
		String result = value;
		
		if(scope==null || result==null) {
			return result;
		}
		
		synchronized(scope) {
			GetOpt getopt = new GetOptImpl(scope);
			
			/*
			 * escape the \ in the line with \\ For a string \\ is the
			 * equivalent of \ (it must be escaped) In regular expressions the \
			 * needs escaped too So \\\\ is the equivalent of \ when it is a
			 * string of a regular expression
			 */
			value = value.replaceAll("\\\\","\\\\\\\\");
			// escape the " in value
			value = value.replaceAll("\"","\\\\\"");
			// enclose it in quotes
			value = "\""+value+"\"";
			getopt.parse(value);			
			result = (String) getopt.getArgumentAt(0).getValue();
			
			return result;
		}
	}
	
	/*******************************************************
	 * Tree Height: 1
	 ******************************************************/	
	
	/**
	 * This is the root of the decission tree. There are two choices: option or value
	 */	
	private void root() {
        logger.debug("root - "+getCurrentChar());
		// get current char and inc index
		String currentChar = getCurrentChar();		
		index++;
		
		if(currentChar == null) {
			// if we are at the end return
	        return;
		}else if("-".equals(currentChar)) {					
			// long or a short option
			option();
		}else {
			// some sort of value
			value(currentChar);
		}
	}

	/*******************************************************
	 * Tree Height: 2
	 ******************************************************/
	
	/**
	 * We are at some sort of option. If it is a short option we have hit
	 * if it is a long we have another -, else it is short. We should also not have any pending values at this point.
	 */
	private void option() {
        logger.debug("option - "+getCurrentChar());
		// get current char and inc index
		String currentChar = getCurrentChar();
		index++;
	
		// if we have options that need a value throw an excpetion
		// this enforces that values follow their options
	    if(!nonFlagOptions.isEmpty()) {
	   	    throw new RuntimeException("Error: Expected value not option at '"+this.commandLine.substring(0,index)+"'");	   
	    }else if(!getGetOpt().getArgumentAt(0).isSet()) {
	    	throw new RuntimeException("Error: Expected command value prior to any options");
	    }else if("-".equals(currentChar)) {
	    	// we have found a long option
			longOption();
	    }else {
			// it is a set of short options
			String shortOptions = nextValue(currentChar," ");
			// for each single character (short option)
			for(int i=0;i<shortOptions.length();i++) {
				// get the option key
				String optionKey = shortOptions.substring(i,i+1);
				// get the option
				Option thisOption = this.getGetOpt().getOption(optionKey);
				// process this option
				this.processOption(thisOption);	
			}			
	    }
	    // we have hit a leaf, return to the root
	    root();
	}	
	
	/**
	 * startSignal should be in signals keys
	 * @param startSignal
	 */
	private void value(String startSignal) {
        logger.debug("value - '"+startSignal+"'");
		// we do not need to inc index since we already did in root
		String value = null;
		
		// if env variable
		// this is needed because we have already passed up the $ and 
		// nextComplexValue cannot process it correctly
		if(startSignal.equals("$")) {
			// get the env variable as a string
			value = String.valueOf(nextEnvVar());		
			index ++;
		}else {
			// is a regular variable (possibly
			String acc = "";
			String endSignal = getEndSignal(startSignal);
			
			// if it was the default start trigger means we want the accumulator to initially be the start trigger
			// else it should not be included
			if(isDefaultSignal(startSignal)) {
				acc = startSignal;
			}
			// get the next complex value
			value = nextComplexValue(acc,endSignal);
		}
		
		// store the value
		storeValue(value);	
		
		// we have hit a leaf, return to the root of the decission tree
		root();
	}
	
	
	/****************************
	 * Tree Height: 3
	 ****************************/
	private void longOption() {
		// get current char and inc index
		String currentChar = getCurrentChar();
		index++;
		
		// the option key is until the next " "
		String optionKey = nextValue(currentChar," ");
		// get the option
		Option thisOption = this.getGetOpt().getOption(optionKey);
		// ensure that this is the long option
		if(!optionKey.equals(thisOption.getLong())) {
			throw new RuntimeException("Error: Got short option '"+optionKey+"' when expected long option at: "+this.commandLine.substring(0,index));
		}
		// process the option
		processOption(thisOption);
	}
	
	/**
	 * This will allow for $, \
	 * 
	 * @param acc the accumulator
	 * @param endSignal what signals the end of this value
	 * @return returns a complex value
	 */	
	private String nextComplexValue(String acc, String endSignal) {
        logger.debug("nextComplexValue( "+acc+","+endSignal+" )"+ endSignal.equals(this.getCurrentChar()));
		// if we are out of bounds and is not the default trigger must terminate with the trigger not out of bounds
        if(isOutOfBounds() && !endSignal.equals(getEndSignal("*"))) {
        	throw new RuntimeException("Error: Expected end trigger '"+endSignal+"'");
        }else if(isOutOfBounds() || endSignal.equals(this.getCurrentChar()) ) {
        	// if we are done
        	
        	// how much do we inc the index, this will change based upon if we are using the default endSignal this is because we do not want to process the " " after something the endSignal
			int inc;			
			if(!endSignal.equals(getEndSignal("*"))) {
				inc = 2;
			}else {
				inc = 1;
			}
			
			index += inc;
			return acc;
		}else {
			// if have not hit the end
			// get thisChar and inc index			
			String thisChar = this.getCurrentChar();
			index++;
			
			// if it is a env variable
			if(thisChar.equals("$")) {
				// acc is itself plus the nextEnvVar
				return nextComplexValue(acc+nextEnvVar(),endSignal);
			}else if(thisChar.equals(ESCAPE_CHAR)) {
				// if we are escaping a value
				return nextComplexValue(acc+escapedValue(),endSignal);
			}else {
				// otherwise get continue
				return nextComplexValue(acc+thisChar,endSignal);	
			}		
		}
	}
	
	/*************************************************************
	 * Tree Height: 4
	 ************************************************************/	
	private String escapedValue() {
		if(isOutOfBounds() ) {
			throw new RuntimeException("Error: Expected escape sequence");
		}else {
			String thisChar = this.getCurrentChar();
			index++;
			
			return thisChar;
		}	
	}	
	

	/*
	 * These are used at multiple levels in the tree
	 */
	
	/**
	 * If commandLine is null or we are out of bounds returns null else the current character as a string 
	 * @return
	 */
	private String getCurrentChar() {
		if(commandLine == null || isOutOfBounds()) {
			return null;
		}
		return this.commandLine.substring(index,index+1);
	}
	
	/**
	 * Gets the next value does NOT consider $ and can be terminated by end of string
	 * @return
	 */	
	private String nextValue(String acc, String endSignal) {
		return nextValue(acc, endSignal,true);
	}
	
	/**
	 * Used to get a value 
	 * @param acc - the accumulator
	 * @param endSignal - what signals the end of the value
	 * @param outOfBoundsTerminates - can out of bounds terminate, or do we insist on the end trigger
	 * @return - the next value
	 */
	private String nextValue(String acc, String endSignal, boolean outOfBoundsTerminates) {
		logger.debug("nextValue( "+acc+","+endSignal+","+outOfBoundsTerminates+" )");
		// are we out of bounds
		if(isOutOfBounds()) {
			// if out of bounds can terminate return
		    if(outOfBoundsTerminates) {
		    	return acc;
		    }else {
		    	// throw an exception otherwise
		    	throw new RuntimeException("Error: Expected endSignal '"+endSignal+"' for sequence '"+acc+"'");
		    }
		}else if (endSignal.equals(this.getCurrentChar()) ) {
			index ++;
			return acc;
		}else {
			// if still need to process
			String thisChar = this.getCurrentChar();
			index++;
			return nextValue(acc+thisChar,endSignal,outOfBoundsTerminates);
		}
	}	
	/**
	 * We should be just after $ when calling this
	 * @return
	 */
	private Object nextEnvVar() {		
		String thisChar = getCurrentChar();
		// gets us past the {
		index++;
		
		// ensure the ENV_PREFIX is after $
		// each Env Var is syntax ${SOME_NAME}
		if(!ENV_PREFIX.equals(thisChar)) {
			throw new RuntimeException("Error: Expected '"+ENV_PREFIX+"' after $ after '"+this.commandLine.substring(0,index)+"'");
		}
		
		// get the name of the env variable, insist on the end trigger terminating
		String envName = this.nextValue("",ENV_SUFFIX,false);
		// return the looked up value
		Object result = null;
		if(this.getGetOpt().getScope() == null) {
			throw new RuntimeException("Error: No scope defined to look up variable '"+envName+"'");
		}else {
		    result = this.getGetOpt().getScope().getValue(envName);
		}
		return result;
	}

	
	/**
	 * Returns if we are out of bounds
	 * @return
	 */
	private boolean isOutOfBounds() {
		return index >= commandLine.length();
	}
	/**
	 * Checks if startSignal would use the default end trigger
	 * @param startSignal - the startSignal to check
	 * @return
	 */
	private boolean isDefaultSignal(String startSignal) {
		return !signals.containsKey(startSignal);
	}
	/**
	 * Get an end trigger for startSignal
	 * @param startSignal - the startSignal to get an endSignal for
	 * @return - if contains key startSignal the value of signals.get(startSignal), else signals.get("*")
	 */
	private String getEndSignal(String startSignal) {
		// if we have this startSignal
		if(signals.containsKey(startSignal)) {
			return (String)signals.get(startSignal);			
		}else {
			return (String)signals.get("*"); // return the default
		}
	}
	
	/**
	 * Store the value in the next available option, or argument
	 * @param value
	 */
	private void storeValue(Object value) {
		Storable var = null;
		// if we have found an option that does not have a value
		if(!this.nonFlagOptions.isEmpty()) {
			var = this.shiftNonFlagOptions();
		}else if(!this.arguments.isEmpty()) {
			// if we have arguments left
			var = this.shiftArgs();
		}else if(this.getGetOpt().isAllowDynamicArgs()) {
			// if allow dynamic arguments
			Argument dynArg =  new ArgumentImpl("a dynamic argument",String.class,false);
			getGetOpt().addArgument(dynArg);
			
			var = dynArg;
		}else {
			throw new RuntimeException("Error: getopt does not accept additional arguments to those specified. "+this.getGetOpt().getArguments());
		}
		
//System.out.println("storing '"+value+"'");
		
		var.setValue(value);
	}
	
	////////////// Options and Args ///////////////////
	
	/**
	 * Processes an option 
	 * @param option - the option to process
	 */
	private void processOption(Option option) {
		// if flag option
		if(option.isFlag()) {
			option.setValue(Option.TRUE); // set to true
		}else {
			// otherwise add to options that need a value
			nonFlagOptions.add(option);
		}
	}
	/**
	 * Return and remove the next argument
	 * @return
	 */
	private Argument shiftArgs() {
		return (Argument)this.arguments.remove(0);
	}
	/**
	 * Return and remove the next nonFlagOption
	 * @return
	 */
	private Option shiftNonFlagOptions() {
		return (Option)this.nonFlagOptions.remove(0);
	}
	
	/**
	 * Used for debug
	 * @param args
	 */
	public static void main(String[] args) {
		Scope scope = new ScopeImpl();
		GetOptImpl getopt = new GetOptImpl(scope);
		
		getopt.isAllowDynamicArgs(false);
		
		getopt.addOptions(new Option[] {
				new OptionImpl("flag option",Boolean.class,"f","flag",true),
				new OptionImpl("storable option",String.class,"s","storable"),
				new OptionImpl("bin stores something",String.class,"b","bin"),
				new OptionImpl("an integer value",Integer.class,"i","integer"),
				new OptionImpl("a double value",Double.class,"d","double"),
				new OptionImpl("a url",java.net.URL.class,null,"u","url"),
				});
		
		ArgParser argparser = new ArgParserImpl();		
		
		getopt.parse("comand_name --url http://www.google.com/ -fsi \"Some string value\" '234' --double 2.34");		
		System.out.println(GetOptImpl.getOptDisplay(getopt));
	}
}
