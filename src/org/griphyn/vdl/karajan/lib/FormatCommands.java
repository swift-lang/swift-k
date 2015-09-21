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
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.swift.catalog.site.Application;
import org.griphyn.vdl.karajan.Command;

public class FormatCommands extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(FormatCommands.class);
	
	private ArgRef<Command[]> commands;
	private ArgRef<String> mode;
	
	private ChannelRef<Object> cr_vargs;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("commands", "mode"), returns("..."));
    }

	@Override
	public Object function(Stack stack) {
        Command[] cmds = this.commands.getValue(stack);
        String mode = this.mode.getValue(stack);
        
        Channel<Object> ret = cr_vargs.get(stack);
        
        if (mode.equals("log")) {
            // only format executable(s) and args
            formatForLog(ret, cmds);
        }
        else if (mode.equals("args")) {
            formatForWrapper(ret, cmds, false);
        }
        else if (mode.equals("paramfile")) {
            formatForWrapper(ret, cmds, true);
        }
        else {
            throw new ExecutionException(this, "Invalid formatting mode: '" + mode + "'");
        }
        return null;
	}

    private void formatForWrapper(Channel<Object> ret, Command[] cmds, boolean paramfile) {
        for (Command cmd : cmds) {
            addOption(ret, paramfile, "e", getExecutable(cmd));
            addOption(ret, paramfile, "out", cmd.getStdout());
            addOption(ret, paramfile, "err", cmd.getStderr());
            addOption(ret, paramfile, "i", cmd.getStdin());
            if (paramfile) {
                for (Object arg : cmd.getArguments()) {
                    addOption(ret, true, "a", arg);
                }
            }
            else {
                ret.add("-a");
                for (Object arg : cmd.getArguments()) {
                    addOption(ret, false, null, arg);
                }
            }
        }
    }

    private String getExecutable(Command cmd) {
        Application app = cmd.getApplication();
        if (app == null || app.getExecutable() == null || app.getExecutable().equals("*")) {
            return cmd.getExecutable();
        }
        else {
            return app.getExecutable();
        }
    }

    private void addOption(Channel<Object> ret, boolean paramfile, String optName, Object value) {
        if (paramfile) {
            ret.add("\n-" + optName);
            if (value != null) {
                ret.add("\n");
                ret.add(String.valueOf(value));
            }
        }
        else {
            if (optName != null) {
                ret.add("-" + optName);
            }
            if (value != null) {
                String s = String.valueOf(value);
                ret.add(s);
                if ("-e".equals(s)) {
                    // escape "-e" so that it's not confused with the next executable
                    ret.add(s);
                }
            }
        }
    }

    private void formatForLog(Channel<Object> ret, Command[] cmds) {
        StringBuilder sb = new StringBuilder();
        if (cmds.length == 1) {
            formatOneForLog(sb, cmds[0]);
        }
        else {
            sb.append("[");
            for (int i = 0; i < cmds.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                formatOneForLog(sb, cmds[i]);
                
            }
            sb.append("]");
        }
        ret.add(sb.toString());
    }

    private void formatOneForLog(StringBuilder sb, Command cmd) {
        sb.append(cmd.getExecutable());
        for (Object arg : cmd.getArguments()) {
            sb.append(' ');
            sb.append(String.valueOf(arg));
        }
    }
}
