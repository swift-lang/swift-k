
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.shell.execution;

import org.globus.cog.abstraction.impl.scheduler.shell.Properties;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

public class TaskHandlerImpl extends
		org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl {
    private Properties props;
    private String name;

	protected DelegatedTaskHandler newDelegatedTaskHandler() {
		return new JobSubmissionTaskHandler(props);
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
	    if (this.name != null) {
	        throw new IllegalStateException("Name is already set to '" + name + "'");
	    }
	    this.name = name;
	    props = Properties.getProperties(name);
	    validateProperties();
	}
	
	private void validateProperties() {
        if (props.getSubmitCommand() == null) {
            throw new IllegalArgumentException("Invalid configuration for provider '" + name + "'. No submit command specified.");
        }
        
        if (props.getRemoveCommand() == null) {
            throw new IllegalArgumentException("Invalid configuration for provider '" + name + "'. No cancel command specified.");
        }
        
        if (props.getPollCommand() == null) {
            throw new IllegalArgumentException("Invalid configuration for provider '" + name + "'. No poll command specified.");
        }
    }
	
	
}