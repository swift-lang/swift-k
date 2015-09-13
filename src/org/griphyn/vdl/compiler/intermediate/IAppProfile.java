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

import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.karajan.Pair;

public class IAppProfile extends AbstractINode {
    private List<Pair<IExpression>> entries;

    public IAppProfile() {
        super();
    }

    public void add(IExpression name, IExpression value) {
        entries = lazyAdd(entries, new Pair<IExpression>(name, value));
    }
    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        for (Pair<IExpression> e : entries) {
            StringTemplate entry = oc.template("map_entry");
            entry.setAttribute("key", e.get(0));
            entry.setAttribute("value", e.get(1));
            st.setAttribute("entries", entry);
        }
    }

    @Override
    protected String getTemplateName() {
        return "swift_attributes";
    }
}
