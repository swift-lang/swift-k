// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 20, 2003
 */
package org.globus.cog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgumentParser {
	public static final int NORMAL = 0;

	public static final int FLAG = 1;

	public static final int OPTIONAL = 2;

	public static final String DEFAULT = null;

    public static final int HIDDEN = 4;

	private Map<String, String> options;

	private Map<String, String> aliases;

	private Set<String> flags;

	private Map<String, String> details;

	private Map<String, String> argumentNames;

	private Map<String, Integer> types;

	private String executableName;

	private List<String> arguments;

	public ArgumentParser() {
		options = new HashMap<String, String>();
		aliases = new HashMap<String, String>();
		flags = new HashSet<String>();
		details = new LinkedHashMap<String, String>();
		argumentNames = new HashMap<String, String>();
		types = new HashMap<String, Integer>();
	}

	public void setExecutableName(String executableName) {
		this.executableName = executableName;
	}

	public void addFlag(String name, String detail, String argName) {
		addOption(name, detail, argName, FLAG + OPTIONAL);
	}

	public void addFlag(String name, String detail) {
		addOption(name, detail, FLAG + OPTIONAL);
	}

	public void addFlag(String name) {
		addOption(name, FLAG + OPTIONAL);
	}
	
	public void addHiddenFlag(String name) {
        addOption(name, FLAG + OPTIONAL + HIDDEN);
    }

	public void addOption(String name, String detail, String argName, int type) {
		if (options.containsKey(name)) {
			if (name == DEFAULT) {
				throw new RuntimeException("Default argument was already added");
			}
			else {
				throw new RuntimeException("Argument " + name + " was already added");
			}
		}
		options.put(name, null);
		setDetail(name, detail);
		setArgumentName(name, argName);
		types.put(name, new Integer(type));
	}

	public void addOption(String name, String detail, int type) {
		addOption(name, detail, null, type);
	}

	public void addOption(String name, int type) {
		addOption(name, null, null, type);
	}

	public void setDetail(String name, String detail) {
		if (options.containsKey(name)) {
			details.put(name, detail);
		}
		else {
			throw new RuntimeException("Trying to set option details for an unexistent option: "
					+ name);
		}
	}

	public void setArgumentName(String name, String arg) {
		if (options.containsKey(name)) {
			argumentNames.put(name, arg);
		}
		else {
			throw new RuntimeException("Trying to set argument name for an unexistent option: "
					+ name);
		}
	}

	public void addAlias(String name, String alias) {
		aliases.put(alias, name);
	}

	public String getStringValue(String name) {
		return options.get(name);
	}

	public String getStringValue(String name, String defaultValue) {
		if (hasValue(name)) {
			return getStringValue(name);
		}
		else {
			return defaultValue;
		}
	}

	public int getIntValue(String name) throws NumberFormatException {
		try {
			return Integer.parseInt(getStringValue(name));
		}
		catch (Exception e) {
			throw new NumberFormatException("Invalid numeric value: " + getStringValue(name));
		}
	}

	public int getIntValue(String name, int defaultValue) throws NumberFormatException {
		if (hasValue(name)) {
			return getIntValue(name);
		}
		else {
			return defaultValue;
		}
	}
	
	public double getFloatValue(String name) throws NumberFormatException {
        try {
            return Double.parseDouble(getStringValue(name));
        }
        catch (Exception e) {
            throw new NumberFormatException("Invalid float value: " + getStringValue(name));
        }
    }

    public double getFloatValue(String name, double defaultValue) throws NumberFormatException {
        if (hasValue(name)) {
            return getFloatValue(name);
        }
        else {
            return defaultValue;
        }
    }

	public boolean isPresent(String name) {
		return flags.contains(name);
	}

	public boolean hasValue(String name) {
		return options.get(name) != null;
	}

	protected boolean hasOption(String name, int option) {
		Integer type = types.get(name);
		if ((type.intValue() & option) != 0) {
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean isOptional(String name) {
		return hasOption(name, OPTIONAL);
	}
	
	protected boolean isHidden(String name) {
        return hasOption(name, HIDDEN);
    }

	protected boolean isFlag(String name) {
		return hasOption(name, FLAG);
	}

	protected String getArgumentName(String name) {
		if (argumentNames.containsKey(name)) {
			return argumentNames.get(name);
		}
		return "value";
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void parse(String[] args) throws ArgumentParserException {
		String lastOption = null;
		for (int i = 0; i < args.length; i++) {
			String crt = args[i];
			String option = crt;
			if (option.startsWith("-") && (lastOption == null)) {
				option = option.substring(1);
				if (aliases.containsKey(option)) {
					option = aliases.get(option);
				}
			}
			if (lastOption != null) {
				if (isFlag(lastOption)) {
					throw new ArgumentParserException("Argument '" + lastOption + "' is a flag");
				}
				options.put(lastOption, option);
				lastOption = null;
				continue;
			}
			if (options.containsKey(option)) {
				flags.add(option);
				if (isFlag(option)) {
					lastOption = null;
				}
				else {
					lastOption = option;
				}
			}
			else {
				if (options.containsKey(DEFAULT) && options.get(DEFAULT) == null) {
					options.put(DEFAULT, crt);
				}
				else if (arguments != null) {
					arguments.add(crt);
				}
				else {
					throw new ArgumentParserException("Unrecognized argument: " + crt);
				}
			}
		}
		if (lastOption != null) {
			throw new ArgumentParserException("Missing value for argument '" + lastOption + "'");
		}
	}

	public void checkMandatory() throws ArgumentParserException {
		for (String name : options.keySet()) {
			if (!isOptional(name) && !isFlag(name) && (options.get(name) == null)) {
				throw new ArgumentParserException("Missing mandatory argument " + name);
			}
		}
	}

	protected boolean hasAliases(String name) {
		for (String alias : aliases.keySet()) {
			if (name.equals(aliases.get(alias))) {
				return true;
			}
		}
		return false;
	}

	public void usage() {
		System.out.println("Usage:");
		if (options.containsKey(DEFAULT)) {
			if (arguments != null) {
			    if (isOptional(DEFAULT)) {
			        System.out.println("  " + executableName + " <options> [" + 
			                getArgumentName(DEFAULT) + " <arguments>]");
			    }
			    else {
			        System.out.println("  " + executableName + " <options> " + 
                            getArgumentName(DEFAULT) + " <arguments>");
			    }
			}
			else {
			    if (isOptional(DEFAULT)) {
			        System.out.println("  " + executableName + " <options> [" + 
			                getArgumentName(DEFAULT) + "]");
			    }
			    else {
			        System.out.println("  " + executableName + " <options> " + 
                            getArgumentName(DEFAULT));
			    }
			}
		}
		else {
			System.out.println("  " + executableName + " <options> ");
		}
		System.out.println("\n\twhere options are:\n");
		for (String name : details.keySet()) {
			String fullName = "";
			if (name == DEFAULT) {
				continue;
			}
			if (isHidden(name)) {
			    continue;
			}
			if (isOptional(name)) {
				fullName += "[";
			}
			if (hasAliases(name)) {
				fullName += "(";
			}
			fullName += "-" + name;
			for (String alias : aliases.keySet()) {
				if (name.equals(aliases.get(alias))) {
					fullName += " | -" + alias;
				}
			}
			if (hasAliases(name)) {
				fullName += ")";
			}
			String detail = details.get(name);
			String arg = argumentNames.get(name);
			System.out.print("    " + fullName);
			if (!isFlag(name)) {
				System.out.print(" <" + getArgumentName(name) + ">");
			}
			if (isOptional(name)) {
				System.out.print("]");
			}
			System.out.println();
			if (detail != null) {
				System.out.println(StringUtil.wordWrap(detail, 6, 65));
			}
			System.out.println();
		}
	}

	public void setArguments(boolean args) {
		if (args) {
			arguments = new ArrayList<String>();
		}
		else {
			arguments = null;
		}
	}
}

