
package org.globus.swift.data.policy;

import java.util.List;

public class Policy {

	public static Policy DEFAULT = valueOf("default");
	
    public Policy()
    {}

    public static Policy valueOf(String token) {
        if (token.compareToIgnoreCase("default") == 0)
            return new Default();
        else if (token.compareToIgnoreCase("direct") == 0)
            return new Direct();

        return null;
    }
}
