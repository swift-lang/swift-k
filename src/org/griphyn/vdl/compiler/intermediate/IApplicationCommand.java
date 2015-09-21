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
 * Created on Sep 16, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

public class IApplicationCommand extends AbstractINode {
    private String executable;
    private List<INode> arguments;
    private IExpression stdin, stdout, stderr;
    
     public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void addArgument(INode iArg) {
        arguments = lazyAdd(arguments, iArg);
    }

    
    public IExpression getStdin() {
        return stdin;
    }

    public void setStdin(IExpression stdin) {
        this.stdin = stdin;
    }


    public IExpression getStdout() {
        return stdout;
    }

    public void setStdout(IExpression stdout) {
        this.stdout = stdout;
    }


    public IExpression getStderr() {
        return stderr;
    }

    public void setStderr(IExpression stderr) {
        this.stderr = stderr;
    }
    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("executable", executable);
        setAll(oc, st, arguments, "arguments");
        
        if (stdin != null) {
            st.setAttribute("stdin", stdin.getTemplate(oc));
        }
        else {
            st.setAttribute("stdin", "null");
        }
        
        if (stdout != null) {
            st.setAttribute("stdout", stdout.getTemplate(oc));
        }
        else {
            st.setAttribute("stdout", "null");
        }
        
        if (stderr != null) {
            st.setAttribute("stderr", stderr.getTemplate(oc));
        }
        else {
            st.setAttribute("stderr", "null");
        }
    }

    @Override
    protected String getTemplateName() {
        return "command";
    }
}
