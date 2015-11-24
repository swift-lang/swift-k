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

public class Program extends AbstractNode {
    private final List<Import> imports;
    private final List<FunctionDeclaration> functionDeclarations;
    private final List<TypeDeclaration> types;
    private final StatementBlock body;
    private String fileName;
    
    public Program() {
        imports = new ArrayList<Import>();
        functionDeclarations = new ArrayList<FunctionDeclaration>();
        types = new ArrayList<TypeDeclaration>();
        body = new StatementBlock();
    }
        
    public void addImport(Import i) {
        imports.add(i);
    }

    public List<Import> getImports() {
        return imports;
    }

    public void addFunctionDeclaration(FunctionDeclaration f) {
        functionDeclarations.add(f);
    }

    public List<FunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }
    
    public void addType(TypeDeclaration t) {
        types.add(t);
    }

    public List<TypeDeclaration> getTypes() {
        return types;
    }
    
    public void addStatement(Statement s) {
        body.addStatement(s);
    }
    
    public StatementBlock getBody() {
        return body;
    }

    @Override
    public List<Node> getSubNodes() {
        List<Node> l = new ArrayList<Node>();
        l.addAll(functionDeclarations);
        l.add(body);
        return l;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getNodeName() {
        return "program";
    }
}
