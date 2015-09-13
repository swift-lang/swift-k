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
 * Created on Sep 3, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.globus.swift.parsetree.Node;

public abstract class AbstractINode implements INode {        
    private int line;
    private Node source;
    
    protected AbstractINode() {
    }
        
    protected void setAll(OutputContext oc, StringTemplate st, List<? extends INode> l, String attrName) {
        if (l != null) {
            for (INode n : l) {
                st.setAttribute(attrName, n.getTemplate(oc));
            }
        }
    }
    
    protected void setAllStr(OutputContext oc, StringTemplate st, List<String> l, String attrName) {
        if (l != null) {
            for (String s : l) {
                st.setAttribute(attrName, s);
            }
        }
    }
    
    protected <T> List<T> lazyAdd(List<T> l, T x) {
        if (l == null) {
            l = new ArrayList<T>();
        }
        l.add(x);
        return l;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
    
    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public StringTemplate getTemplate(OutputContext oc) {
        StringTemplate st = makeStringTemplate(oc);
        setTemplateAttributes(oc, st);
        return st;
    }

    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        if (line != 0) {
            st.setAttribute("line", line);
        }
    }

    protected StringTemplate makeStringTemplate(OutputContext oc) {
        return oc.template(getTemplateName());
    }

    protected abstract String getTemplateName();
}
