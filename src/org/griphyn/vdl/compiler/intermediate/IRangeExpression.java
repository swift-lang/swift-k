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

import org.antlr.stringtemplate.StringTemplate;

public class IRangeExpression extends IAbstractExpression {
    private IExpression from, to, step;
    
    public IRangeExpression() {
        super();
    }


    public IExpression getFrom() {
        return from;
    }

    public void setFrom(IExpression from) {
        this.from = from;
    }


    public IExpression getTo() {
        return to;
    }

    public void setTo(IExpression to) {
        this.to = to;
    }


    public IExpression getStep() {
        return step;
    }

    public void setStep(IExpression step) {
        this.step = step;
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("from", from.getTemplate(oc));
        st.setAttribute("to", to.getTemplate(oc));
        if (step != null) {
            st.setAttribute("step", step.getTemplate(oc));
        }
    }

    @Override
    protected String getTemplateName() {
        return "range";
    }
}
