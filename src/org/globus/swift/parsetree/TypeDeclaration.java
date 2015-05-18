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
import java.util.Collections;
import java.util.List;

public class TypeDeclaration extends AbstractNode {
    private String name;
    private String typeAlias;
    private final List<TypeMemberDeclaration> members;
    
    public TypeDeclaration() {
        members = new ArrayList<TypeMemberDeclaration>();
    }

    public void addMember(TypeMemberDeclaration mdef) {
        members.add(mdef);
    }

    public List<TypeMemberDeclaration> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeAlias() {
        return typeAlias;
    }

    public void setTypeAlias(String typeAlias) {
        this.typeAlias = typeAlias;
    }
    
    @Override
    public List<? extends Node> getSubNodes() {
        return Collections.emptyList();
    }
    
    @Override
    public String getNodeName() {
        return "type declaration";
    }
}
