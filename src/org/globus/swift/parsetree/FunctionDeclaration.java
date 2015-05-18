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
 * Created on Apr 17, 2015
 */
package org.globus.swift.parsetree;

import java.util.ArrayList;
import java.util.List;

public class FunctionDeclaration extends AbstractNode {
    private String name;
    private List<FormalParameter> returns;
    private List<FormalParameter> parameters;
    private final StatementBlock body;
    
    public FunctionDeclaration() {
        returns = new ArrayList<FormalParameter>();
        parameters = new ArrayList<FormalParameter>();
        body = new StatementBlock();
    }

    public void addReturn(FormalParameter param) {
        returns.add(param);
    }
    
    public void addParameter(FormalParameter param) {
        parameters.add(param);
    }

    public void setReturns(List<FormalParameter> returns) {
        this.returns = returns;
    }

    public void setParameters(List<FormalParameter> parameters) {
        this.parameters = parameters;
    }

    public List<FormalParameter> getReturns() {
        return returns;
    }

    public List<FormalParameter> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StatementBlock getBody() {
        return body;
    }

    @Override
    public List<? extends Node> getSubNodes() {
        return parameters;
    }
    
    @Override
    public String getNodeName() {
        return "function declaration (" + name + ")";
    }
}
