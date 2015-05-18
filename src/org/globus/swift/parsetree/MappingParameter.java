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

import java.util.List;


public class MappingParameter extends AbstractNode {
    private String name;
    private Expression value;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Expression getValue() {
        return value;
    }
    
    public void setValue(Expression value) {
        this.value = value;
    }
    
    @Override
    public List<? extends Node> getSubNodes() {
        return list(value);
    }
    
    @Override
    public String getNodeName() {
        return "mapping parameter";
    }
}
