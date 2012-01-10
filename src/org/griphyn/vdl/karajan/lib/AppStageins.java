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
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.globus.swift.data.Director;
import org.globus.swift.data.policy.Policy;
import org.griphyn.vdl.mapping.AbsFile;

public class AppStageins extends AbstractSequentialWithArguments {

    static Logger logger = Logger.getLogger(AppStageins.class);
    
    public static final Arg JOBID = new Arg.Positional("jobid");
    public static final Arg FILES = new Arg.Positional("files");
    public static final Arg DIR = new Arg.Positional("dir");
    public static final Arg STAGING_METHOD = new Arg.Positional("stagingMethod");
    public static final Arg.Channel STAGEIN = new Arg.Channel("stagein");

    static {
        setArguments(AppStageins.class, new Arg[] { JOBID, FILES, DIR,
                STAGING_METHOD });
    }

    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(FILES.getValue(stack));
        for (Object f : files) {
            AbsFile file = new AbsFile(TypeUtil.toString(f));
            Policy policy = Director.lookup(file.toString());
            if (policy != Policy.DEFAULT) {
                logger.debug("will not stage in (CDM): " + file);
                continue; 
            }
                                        
            String protocol = file.getProtocol();
            if (protocol.equals("file")) {
                protocol = TypeUtil.toString(STAGING_METHOD.getValue(stack));
            }
            String path = file.getDir().equals("") ? file.getName() : file
                .getDir()
                    + "/" + file.getName();
            String relpath = path.startsWith("/") ? path.substring(1) : path;
            if (logger.isDebugEnabled()) {
                logger.debug("will stage in: " + relpath + " via: " + protocol);
            }
            ArgUtil.getChannelReturn(stack, STAGEIN).append(
                makeList(protocol + "://" + file.getHost() + "/" + path,
                    TypeUtil.toString(DIR.getValue(stack)) + "/" + relpath));
        }
        super.post(stack);
    }

    private List<String> makeList(String s1, String s2) {
        List<String> l = new LinkedList<String>();
        l.add(s1);
        l.add(s2);
        return l;
    }
}
