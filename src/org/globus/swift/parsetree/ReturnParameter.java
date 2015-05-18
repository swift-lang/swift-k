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

import java.util.Collections;
import java.util.List;

public class ReturnParameter extends AbstractNode {
    private LValue lValue;
    private String type;
    private String binding;
    
    public LValue getLValue() {
        return lValue;
    }
    
    public void setLValue(LValue lValue) {
        this.lValue = lValue;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    @Override
    public List<? extends Node> getSubNodes() {
        return Collections.emptyList();
    }
    
    @Override
    public String getNodeName() {
        return "return parameter";
    }
}
