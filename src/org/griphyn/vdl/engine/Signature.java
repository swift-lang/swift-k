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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.globus.swift.parsetree.FormalParameter;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;


public class Signature {
    public static enum InvocationType {
        INTERNAL, USER_DEFINED;
    }
        
    public static class Parameter {
        private String name;
        private Type type;
        private boolean optionalArg;
            
        public Parameter(Type type, String name) {
            this.type = type;
            this.name = name;
            this.optionalArg = false;
        }
            
        public Parameter(Type type) {
            this.type = type;
            this.optionalArg = false;
        }
        
        public String getName() {
            return name;        
        }
        
        public Type getType() {
            return type;        
        }
            
        public boolean isOptional() {
            return optionalArg;
        }
        
        public void setOptional(boolean optionalArg) {
            this.optionalArg = optionalArg;
        }
        
        public String toString() {
            if (name != null) {
                if (optionalArg) {
                    return type + " " + name + " = ...";
                }
                else {
                    return type.toString();
                }
            }
            else {
                return type.toString();
            }
        }
    }
    
	private String name;
	private String mangledName;
	private List<Parameter> parameters;
	private List<Parameter> returns;
	private List<TypeParameter> typeParameters;
	private boolean varArgs, optionals, returnsHaveNames;
	private Type varArgsType;
	private InvocationType invocationType;
	private boolean isProcedure;
	private int positionalCount;
	
	private boolean deprecated;

	public Signature(String name) {
		this.name = name;
		parameters = new ArrayList<Parameter>();
		returns = new ArrayList<Parameter>();
		varArgs = false;
		invocationType = InvocationType.USER_DEFINED;
	}

	public String getName() {
		return name;
	}
	
	public String getMangledName() {
		return mangledName;
	}
	
	public void usePlainName() {
		mangledName = name;
    }
	
	public void useMangledName() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("$");
        for (Parameter s : parameters) {
        	sb.append(mangleType(s.getType()));
        	sb.append("$");
        }
        mangledName = sb.toString();
    }

	private String mangleType(Type type) {
	    StringBuilder sb = new StringBuilder();
        String tn = type.toString();
        for (int i = 0; i < tn.length(); i++) {
            char c = tn.charAt(i);
            switch (c) {
                case '[':
                    sb.append('#');
                    break;
                case ']':
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public void addInputArg(Parameter inputArg) {
		if (inputArg.isOptional()) {
		    parameters.add(inputArg);
		    optionals = true;
		}
		else {
		    // insert before optionals
            parameters.add(positionalCount, inputArg);
            positionalCount++;
		}
	}

	public void addOutputArg(Parameter outputArg) {
		returns.add(outputArg);
		if (outputArg.getName() != null) {
		    returnsHaveNames = true;
		}
	}

	public boolean hasVarArgs() {
        return varArgs;
    }

    public void setVarArgs(boolean varArgs) {
        this.varArgs = varArgs;
    }

    public Type getVarArgsType() {
        return varArgsType;
    }

    public void setVarArgsType(Type varArgsType) {
        this.varArgsType = varArgsType;
    }
    
    public boolean hasOptionalArgs() {
        return optionals;
    }
    
    public boolean isOptionalArg(String name) {
        for (Parameter arg : parameters) {
            if (name.equals(arg.getName()) && arg.isOptional()) {
                return true;
            }
        }
        return false;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    public List<Parameter> getReturns() {
        return returns;
    }
    
    public int getReturnIndex(String name) {
        for (int i = 0; i < returns.size(); i++) {
            if (name.equals(returns.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
    
    public int getParameterIndex(String name) {
        for (int i = 0; i < parameters.size(); i++) {
            if (name.equals(parameters.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
    
    public Parameter getParameter(String name) {
        int index = getParameterIndex(name);
        if (index == -1) {
            return null;
        }
        else {
            return  parameters.get(index);
        }
    }


	public void setInputArgs(List<FormalParameter> fp, Types types) throws NoSuchTypeException {
		for (int i = 0; i < fp.size(); i++) {
		    FormalParameter p = fp.get(i); 
		    Type t = getType(types, p.getType());
			Parameter fas = new Parameter(t, p.getName());
			boolean optional = p.getDefaultValue() != null;
			fas.setOptional(optional);
			this.addInputArg(fas);
		}
	}

	public void setOutputArgs(List<FormalParameter> fp, Types types) throws NoSuchTypeException {
		for (int i = 0; i < fp.size(); i++) {
		    FormalParameter p = fp.get(i);
		    Type t = getType(types, p.getType());
		    String name = p.getName();
			Parameter fas = new Parameter(t, name);
			this.addOutputArg(fas);
		}
	}
	
	private Type getType(Types types, String name) throws NoSuchTypeException {
	    if (typeParameters != null) {
	        for (TypeParameter tp : typeParameters) {
	            if (name.equals(tp.getName())) {
	                return tp;
	            }
	        }
	    }
        return types.getType(name);
    }

    public InvocationType getType() {
        return invocationType;
    }

    public void setType(InvocationType type) {
        this.invocationType = type;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (typeParameters != null) {
            sb.append('<');
            appendList(sb, typeParameters);
            sb.append("> ");
        }
        sb.append('(');
        appendList(sb, returns);
        sb.append(") ");
        sb.append(name);
        sb.append('(');
        appendList(sb, parameters);
        if (varArgs) {
            if (parameters.size() > 0) {
                sb.append(", ");
            }
            sb.append(varArgsType);
            sb.append(" ...");
        }
        sb.append(')');
        return sb.toString();
	}

    private void appendList(StringBuilder sb, List<?> l) {
        Iterator<?> it = l.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
    }

    public void setTypeParameters(TypeParameter[] typeParams) {
        this.typeParameters = Arrays.asList(typeParams);
    }

    public boolean returnsHaveNames() {
        return returnsHaveNames;
    }

    public boolean hasReturn(String name, Type type) {
        for (Parameter r : returns) {
            if (name.equals(r.getName())) {
                return r.getType().canBeAssignedTo(type);
            }
        }
        return false;
    }

    public Parameter getParameter(int i) {
        return parameters.get(i);
    }

    public int getPositionalCount() {
        return positionalCount;
    }

    public boolean hasPositional(String name, Type type) {
        return hasParam(name, type, false);
    }
    
    public boolean hasOptional(String name, Type type) {
        return hasParam(name, type, true);
    }

    private boolean hasParam(String name, Type type, boolean optional) {
        for (Parameter p : parameters) {
            if (name.equals(p.getName())) {
                if (p.isOptional() != optional) {
                    return false;
                }
                return p.getType().isAssignableFrom(type);
            }
        }
        return false;
    }

    public void clearTypeParameterBindings() {
        if (typeParameters == null) {
            return;
        }
        for (TypeParameter tp : typeParameters) {
            tp.clearBinding();
        }
    }

    public Type getReturnType() {
        if (returns == null || returns.size() != 1) {
            throw new RuntimeException("Internal error: return count != 1");
        }
        return returns.get(0).getType();
    }

    public boolean isProcedure() {
        return isProcedure;
    }

    public void setIsProcedure(boolean isProcedure) {
        this.isProcedure = isProcedure;
    }
}
