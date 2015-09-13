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

/*
 * Created on Apr 19, 2015
 */
package org.globus.swift.parsetree;

import java.util.ArrayList;
import java.util.List;

public class MappingDeclaration extends AbstractNode {
    private String descriptor;
    private final List<MappingParameter> parameters;
    
    public MappingDeclaration() {
        parameters = new ArrayList<MappingParameter>();
    }
    
    public MappingDeclaration(String descriptor, MappingParameter... params) {
        this();
        this.descriptor = descriptor;
        for (MappingParameter param : params) {
            this.parameters.add(param);
        }
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
    
    public void addParameter(MappingParameter mp) {
        parameters.add(mp);
    }

    public List<MappingParameter> getParameters() {
        return parameters;
    }
    
    @Override
    public List<? extends Node> getSubNodes() {
        return parameters;
    }
    
    @Override
    public String getNodeName() {
        return "mapping declaration";
    }
}
