/*
 * 
 */
package org.globus.cog.gridshell.getopt.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.globus.cog.gridshell.getopt.interfaces.ArgParser;
import org.globus.cog.gridshell.getopt.interfaces.Argument;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.getopt.interfaces.Option;
import org.globus.cog.gridshell.getopt.interfaces.Storable;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;

/**
 * An implmentation of GetOpt
 * 
 * 
 */
public class GetOptImpl implements GetOpt {
	// the Scope for this getopt, used for variables
	private Scope scope;
	// the arg parser for this getopt
	private ArgParser argParser;
	// the commandline value
	private String commandLineValue;
	// the description for this command
	private String description;
	// options that are associated with this command
	private Map options = new HashMap();
	// arguments associated with this command
	private List args = new ArrayList();
	// will we allow dynamic arguments
	private boolean allowDynamicArgs = true;
	// will we allow dynamic options
	private boolean allowDyanmicOptions = false;
	// has this GetOpt been used, we only use each instance once
	private boolean hasBeenUsed = false;
		
	/**
	 * Create an instance with the specified scope
	 * @param scope - specifies a scope
	 * @param argParser - allows to specify a different argParser so other features can be used
	 */
	public GetOptImpl(Scope scope, ArgParser argParser) {
		this.scope = scope;
		
		// add our an argument for the command
		addArgument(new ArgumentImpl("the name of the command",String.class,true));
		
		this.argParser = argParser;
	}
	/**
	 * Convenience Constructor, adds our default ArgParserImpl
	 * @param scope
	 */
	public GetOptImpl(Scope scope) {
		this(scope,new ArgParserImpl());
	}
	
	public GetOptImpl() {
		this(new ScopeImpl());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#addArgument(org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Argument)
	 */
	public void addArgument(Argument arg) {
		args.add(arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#addArguments(org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Argument[])
	 */
	public void addArguments(Argument[] args) {
		if(args != null) {
			for(int i=0;i<args.length;i++) {
				this.addArgument(args[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#addOption(org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option)
	 */
	public void addOption(Option option) {
		if(option == null)
			return;
		if(!containsOption(option)) {
			String sKey = option.getShort();
			String lKey = option.getLong();
			
			if(sKey != null) {
				options.put(sKey,option);
			}
			if(lKey != null) {
			    options.put(lKey,option);
			}
		}else {
			throw new RuntimeException("Option '"+option+"' already exists as '"+getOption(option.getShort())+"'");
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#addOptions(org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option[])
	 */
	public void addOptions(Option[] options) {
		if(options != null) {
			for(int i=0;i<options.length;i++) {
				addOption(options[i]);
			}
		}
	}	
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#containsOption(org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option)
	 */
	public boolean containsOption(Option option) {
		if(option == null) {
			return false;
		}
		return this.options.containsKey(option.getLong()) || options.containsKey(option.getShort());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getArguments()
	 */
	public List getArguments() {
		List result = new ArrayList();
		result.addAll(this.args);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getArgumentAt(int)
	 */
	public Argument getArgumentAt(int index) {
		return (Argument)this.args.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getCommandLineValue()
	 */
	public String getCommandLineValue() {
		return commandLineValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getScope()
	 */
	public Scope getScope() {
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#setScope(org.globus.cog.gridface.impl.gridshell.interfaces.Scope)
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getOptions()
	 */
	public Set getOptions() {
		Set result = new TreeSet();
		result.addAll(this.options.values());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#getOption(java.lang.String)
	 */
	public Option getOption(String key) {
		if(key == null){
			return null;
		}else if(!options.containsKey(key)) {
			// are we allowing dynamic options?
			if(this.isAllowDynamicOptions()) {				
				if(key.length() == 1) {
					// it is a short dynamic option
					addOption(new OptionImpl("Dyanmic Option",java.lang.String.class,key,null));
				}else {
					// it is a long dynammic option
					addOption(new OptionImpl("Dyanmic Option",java.lang.String.class,null,key));
				}
				// call ourself to return the produced dyanmic option
				return getOption(key);
			}else {
				throw new RuntimeException("Option '"+key+"' is not defined");
			}		    
		}else {
		    return (Option)options.get(key);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#parse(java.lang.String)
	 */
	public synchronized void parse(String commandLineValue) {
		if(this.hasBeenUsed) {
			throw new RuntimeException("Error: You can only parse once with each instance. Create a new GetOpt Instance");
		}else {
			this.hasBeenUsed = !this.hasBeenUsed;
		}
		
		this.commandLineValue = commandLineValue;
		this.argParser.parse(this,commandLineValue);		
	}
	
	public void checkRequired() {
		// check required args/options
		Collection requiredNotSet = getRequiredNotSet();
		if(!requiredNotSet.isEmpty()) {
			throw new RuntimeException("Error: The following values are required and not set: "+requiredNotSet);
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#isStrictArgs()
	 */
	public boolean isAllowDynamicArgs() {
		return allowDynamicArgs;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#isStrict(boolean)
	 */
	public void isAllowDynamicArgs(boolean value) {
		allowDynamicArgs = value;
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#isAllowDynamicOptions()
	 */
	public boolean isAllowDynamicOptions() {
		return allowDyanmicOptions;
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#isAllowDynamicOptions(boolean)
	 */
	public void isAllowDynamicOptions(boolean value) {
		this.allowDyanmicOptions = value;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.GetOpt#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;		
	}	
	
	/**
	 * Returns a collection of the required Storables (options and arguments) that are not set.
	 * @return
	 */
	private Collection getRequiredNotSet() {
		Collection result = new LinkedList();
		Collection storables = this.getArguments();
		storables.addAll(getOptions());
		
		Iterator iStorables = storables.iterator();
		while(iStorables.hasNext()) {
			Storable s = (Storable)iStorables.next();
			if(!s.isSet() && s.isRequired()) {
				result.add(s);
			}
		}
		
		return result;
	}
	
	/**
	 * Used just to display for debug
	 * @param getOpt - the getopt to display
	 * @return
	 */
	public static String getOptDisplay(GetOpt getOpt) {
		String result = getOpt.getCommandLineValue()+":\n";
		
		Iterator iItems =  getOpt.getOptions().iterator();
		while(iItems.hasNext()) {
			result += iItems.next()+"\n";
		}
		iItems =  getOpt.getArguments().iterator();
		while(iItems.hasNext()) {
			result += iItems.next()+"\n";
		}
		
		return result;		
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.getopt.interfaces.GetOpt#isOptionSet(java.lang.String)
	 */
	public boolean isOptionSet(String name) {
		if(this.options.containsKey(name)) {
			Option option = (Option)options.get(name);
			if(option != null) {
				return option.isSet();
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridshell.getopt.interfaces.GetOpt#isArgSet(int)
	 */
	public boolean isArgSet(int index) {
		if(index < this.args.size()) {
			Argument arg = (Argument)args.get(index);
			if(arg != null) {
				return arg.isSet();
			}
		}
		return false;
	}
}