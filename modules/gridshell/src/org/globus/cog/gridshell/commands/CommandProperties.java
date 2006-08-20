/*
 * 
 */
package org.globus.cog.gridshell.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeException;
import org.globus.cog.gridshell.model.ScopeableProperties;

/**
 * <p>
 * A class used to create commands from a properties file
 * </p>
 * <p>
 * Example configuration file:
 * </p>
 * <code> 
 * # some inherited files<br/>
 * gridshell.command.inherited.1=${this.path.string}/inherited-1.properties<br/>
 * gridshell.command.inherited.2=${this.path.string}/inherited-2.properties
 * <br/>
 * # some commands<br/>
 * gridshell.command.id.1.org.globus.cog.gridshell.command.gsh.clear=my clear command<br/>
 * gridshell.command.id.2.org.globus.cog.gridshell.command.gsh.clear=clear<br/>
 * gridshell.command.id.1.org.globus.cog.gridshell.command.gsh.echo=echo<br/>
 * # &lt;file-name&gt; should be a value that can be passed to new File(&lt;file-name&gt;)<br/>
 * gridshell.command.inherit.1=&lt;file-name&gt;
 * gridshell.command.inherit.2=&lt;file-name&gt;
 * </code>
 * 
 *  
 */
public class CommandProperties extends ScopeableProperties {
	private static final Logger logger = Logger.getLogger(CommandProperties.class);
		
	public final static String PREFIX_INHERIT = "gridshell.command.inherit.";
	public final static String PREFIX_COMMAND = "gridshell.command.id.";
	public final static String PREFIX_MAN = "gridshell.command.man.";
	
	public CommandProperties(File file) throws IOException, ScopeException {		
		super(file,PREFIX_INHERIT);
	}
	public String getCommandValue(String id) {
	    return this.getProperty(PREFIX_COMMAND+id);
	}
	public Scope getManPages() {
	    return this.getSubScope(PREFIX_MAN);
	}
	public Scope getCommands() {
	    return this.getSubScope(PREFIX_COMMAND);
	}
	public Collection getClasses() {
	    return this.getSubScope(PREFIX_COMMAND).getUniqueValues();
	}
}
