
package org.globus.swift.data.policy;

import java.util.List;

public abstract class Policy {

	public static Policy DEFAULT = valueOf("default");
	
    public Policy()
    {}

    public abstract void settings(List<String> tokens);
    
    public static Policy valueOf(String token) {
        if (token.compareToIgnoreCase("default") == 0)
            return new Default();
        else if (token.compareToIgnoreCase("direct") == 0)
            return new Direct();
        else if (token.compareToIgnoreCase("local") == 0)
            return new Local();
        else if (token.compareToIgnoreCase("broadcast") == 0)
            return new Broadcast();
        else if (token.compareToIgnoreCase("gather") == 0)
            return new Gather();
        else if (token.compareToIgnoreCase("external") == 0)
            return new External();
        return null;
    }
}
