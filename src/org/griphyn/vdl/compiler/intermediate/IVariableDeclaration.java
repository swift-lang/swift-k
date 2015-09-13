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
import org.griphyn.vdl.engine.VariableScope;
import org.griphyn.vdl.type.Type;

public class IVariableDeclaration extends AbstractINode implements IRefCounted {
    private String name;
    private Type type;
    private String field;
    private boolean global;
    private boolean input;
    private String file;
    private IMapping mapping;
    private boolean unused;
    private int waitCount;
    private int readCount;
    private int readRefIndex;

    public IVariableDeclaration() {
        super();
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }


    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }


    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }


    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }


    public IMapping getMapping() {
        return mapping;
    }

    public void setMapping(IMapping mapping) {
        this.mapping = mapping;
    }
    

    public boolean getUnused() {
        return unused;
    }

    public void setUnused(boolean unused) {
        this.unused = unused;
    }

    public void incWriteCount(int amount) {
    	if (amount == VariableScope.FULL_WRITE_COUNT) {
    		waitCount = VariableScope.FULL_WRITE_COUNT;
    	}
    	else {
    	    waitCount += amount;
    	}
    }
    
    public void incReadCount(int amount) {
        readCount += amount;
    }
    
    public int getReadCount() {
        return readCount;
    }
    
    @Override
    public int getWriteCount() {
        return waitCount;
    }


    public int getReadRefIndex() {
        return readRefIndex;
    }

    public void setReadRefIndex(int readRefIndex) {
        this.readRefIndex = readRefIndex;
    }

    @Override
    public boolean isWrapped() {
        return readCount > 0;
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("name", name);
        st.setAttribute("type", type.toString());
        st.setAttribute("field", field);
        st.setAttribute("isGlobal", global);
        if (input) {
            st.setAttribute("input", "true");
        }
        if (file != null) {
            StringTemplate fileST = new StringTemplate("file");
            fileST.setAttribute("name", file);
            fileST.defineFormalArgument("params");
            st.setAttribute("file", fileST);
        }
        if (mapping != null) {
            st.setAttribute("mapping", mapping.getTemplate(oc));
        }
        if (unused) {
            st.setAttribute("unused", true);
        }
        if (waitCount > 0) {
        	if (waitCount != VariableScope.FULL_WRITE_COUNT) {
        	    st.setAttribute("waitCount", waitCount);
        	}
        }
        if (readCount > 0) {
        	/*
        	 * If the variable is wrapped, then cleaning cannot happen until
        	 * all reads AND writes are done. This means we need to wrap
        	 * all access (read and write) to this variable in read-ref-decrementing
        	 * accessors and count both read and write access as read-ref counts.
        	 */
        	if (waitCount == VariableScope.FULL_WRITE_COUNT) {
        		// one big write counts as one read
        		st.setAttribute("readCount", readCount + 1);
        	}
        	else {
        	    st.setAttribute("readCount", readCount + waitCount);
        	}
        }
    }


    @Override
    protected String getTemplateName() {
        return "variable";
    }
}
