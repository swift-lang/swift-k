package org.griphyn.vdl.engine;

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
		this.type = type;
		this.name = name;
		this.anyType = false;
		this.optionalArg = false;
	}
	
	public FormalArgumentSignature(boolean anyType) {
		this.anyType = anyType;
		this.optionalArg = false;
	}
	
	public FormalArgumentSignature(String type) {
		this.type = type;
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
