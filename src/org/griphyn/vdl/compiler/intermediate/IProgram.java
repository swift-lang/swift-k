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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateWriter;
import org.griphyn.vdl.engine.VariableScope;

public class IProgram extends AbstractIStatementContainer {
    private String buildVersion;
    
    private String libraryVersion;
    private List<INode> constants;
    private List<ITypeDefinition> types;
    private List<IProcedureDeclaration> procedures;

    public IProgram(VariableScope scope) {
        super(scope);
        constants = new ArrayList<INode>();
        types = new ArrayList<ITypeDefinition>();
        procedures = new ArrayList<IProcedureDeclaration>();
    }
    

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }
    
    
    public String getLibraryVersion() {
        return libraryVersion;
    }

    public void setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
    }
    
    
    public void addConstant(INode constant) {
        this.constants.add(constant);
    }
    
    public void addType(ITypeDefinition type) {
        this.types.add(type);
    }

    public void addProcedureDefinition(IProcedureDeclaration proc) {
        this.procedures.add(proc);
    }
    

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("buildversion", buildVersion);
        st.setAttribute("stdlibversion", libraryVersion);
        setAll(oc, st, constants, "constants");
        setAll(oc, st, types, "types");
        setAll(oc, st, procedures, "procedures");
    }


    @Override
    protected String getTemplateName() {
        return "program";
    }


    public void writeTo(OutputContext oc, final PrintStream ps) throws IOException {
        StringTemplateWriter w = new AutoIndentWriter(new Writer() {
            @Override
            public Writer append(char c) throws IOException {
                ps.append(c);
                return this;
            }

            @Override
            public Writer append(CharSequence csq, int start, int end)
                    throws IOException {
                ps.append(csq, start, end);
                return this;
            }

            @Override
            public Writer append(CharSequence csq) throws IOException {
                ps.append(csq);
                return this;
            }

            @Override
            public void close() throws IOException {
                // don't close here; let the caller deal with that 
                ps.flush();
            }

            @Override
            public void flush() throws IOException {
                ps.flush();
            }

            @Override
            public void write(char[] buf, int start, int len) throws IOException {
                for (int i = 0, j = start; i < len; i++, j++) {
                    ps.print(buf[j]);
                }
            }

            @Override
            public void write(char[] cbuf) throws IOException {
                ps.print(cbuf);
            }

            @Override
            public void write(int c) throws IOException {
                ps.print((char) c);
            }

            @Override
            public void write(String s, int start, int len)
                    throws IOException {
                for (int i = start; i < start + len; i++) {
                    ps.write(s.charAt(i));
                }
            }

            @Override
            public void write(String str) throws IOException {
                ps.print(str);
            }
        });
        
        getTemplate(oc).write(w);
    }
}
