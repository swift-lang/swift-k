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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public abstract class AbstractNode implements Node {
    private int line;
    
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
    
    protected List<? extends Node> list(Node n) {
        return Collections.singletonList(n);
    }
    
    protected List<? extends Node> list(Node n1, Node n2) {
        return Arrays.asList(new Node[] {n1, n2});
    }
    
    protected List<? extends Node> list(Node n1, Node n2, Node n3) {
        return Arrays.asList(new Node[] {n1, n2, n3});
    }
    
    public String toString() {
        return getNodeName() + " " + this.getSubNodes();
    }
}
