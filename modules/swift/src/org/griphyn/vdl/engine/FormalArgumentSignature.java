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


package org.griphyn.vdl.engine;

import org.griphyn.vdl.type.Types;

public class FormalArgumentSignature {
	private String name;
	private String type;
	private boolean anyType;
	private boolean optionalArg;
	
	
	public FormalArgumentSignature() {			
		this.anyType = false;
		this.optionalArg = false;
	}
	
	public FormalArgumentSignature(String type, String name) {
		this.type = Types.normalize(type);
		this.name = name;
		this.anyType = false;
		this.optionalArg = false;
	}
	
	public FormalArgumentSignature(boolean anyType) {
		this.anyType = anyType;
		this.optionalArg = false;
		if (anyType) {
		    this.type = ProcedureSignature.ANY;
		}
	}
	
	public FormalArgumentSignature(String type) {
		this.type = Types.normalize(type);
		this.anyType = false;
		this.optionalArg = false;
	}
	
	public String getName() {
		return name;		
	}
	
	public String getType() {
		return type;		
	}
	
	public boolean isAnyType() {
		return anyType;		
	}
	
	public boolean isOptional() {
		return optionalArg;
	}
	
	public void setOptional(boolean optionalArg) {
		this.optionalArg = optionalArg;
	}
	
	public String toString() {
	    return type + " " + name;
	}
}
