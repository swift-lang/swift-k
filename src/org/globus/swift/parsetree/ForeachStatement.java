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

import java.util.List;

public class ForeachStatement extends Statement {
    private String var;
    private String indexVar;
    private Expression inExpression;
    private final StatementBlock body;

    public ForeachStatement() {
        body = new StatementBlock();
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getIndexVar() {
        return indexVar;
    }

    public void setIndexVar(String indexVar) {
        this.indexVar = indexVar;
    }

    public Expression getInExpression() {
        return inExpression;
    }

    public void setInExpression(Expression inExpression) {
        this.inExpression = inExpression;
    }

    public StatementBlock getBody() {
        return body;
    }

    @Override
    public List<? extends Node> getSubNodes() {
        return list(inExpression, body);
    }
    
    @Override
    public String getNodeName() {
        return "foreach statement";
    }
}
