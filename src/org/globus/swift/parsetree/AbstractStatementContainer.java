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

public abstract class AbstractStatementContainer extends AbstractNode implements StatementContainer {
    private final List<Statement> statements;
    private final List<VariableDeclaration> variableDeclarations;
    
    public AbstractStatementContainer() {
        statements = new ArrayList<Statement>();
        variableDeclarations = new ArrayList<VariableDeclaration>();
    }

    public void addStatement(Statement s) {
        statements.add(s);
    }

    public List<Statement> getStatements() {
        return statements;
    }
    
    public void addVariableDeclaration(VariableDeclaration vd) {
        variableDeclarations.add(vd);
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return variableDeclarations;
    }
    
    @Override
    public List<? extends Node> getSubNodes() {
        return statements;
    }
}
