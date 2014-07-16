/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



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
