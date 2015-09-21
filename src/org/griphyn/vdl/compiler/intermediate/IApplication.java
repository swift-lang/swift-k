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

public class IApplication extends AbstractINode {
    private IAppProfile profile;
    private List<IApplicationCommand> commands;

    public IApplication() {
        super();
        commands = new ArrayList<IApplicationCommand>();
    }
 
    public IAppProfile getProfile() {
        return profile;
    }

    public void setProfile(IAppProfile profile) {
        this.profile = profile;
    }
    
    public void addCommand(IApplicationCommand cmd) {
        commands.add(cmd);
    }

    public List<IApplicationCommand> getCommands() {
        return commands;
    }

    @Override
    protected StringTemplate makeStringTemplate(OutputContext oc) {
        return new StringTemplate("application");
    }
    
    @Override
    protected String getTemplateName() {
        // not used
        return null;
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        setAll(oc, st, commands, "commands");
        if (profile != null) {
            st.setAttribute("attributes", profile.getTemplate(oc));
        }
    }
}
