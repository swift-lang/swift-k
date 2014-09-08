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


package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
import org.globus.cog.abstraction.impl.execution.coaster.WorkerShellCommand;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Terminal;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Terminal.InputHandler;
import org.ietf.jgss.GSSCredential;

public class WorkerTerminalInputHandler implements InputHandler {
    public static final Logger logger = Logger
        .getLogger(WorkerTerminalInputHandler.class);

    private Dialog dialog;
    private Terminal term;
    private Task task;
    private String workerId, contact;
    private GSSCredential cred;

    public WorkerTerminalInputHandler(Dialog dialog, Terminal term, Task task,
            String workerId) {
        this.dialog = dialog;
        this.term = term;
        this.task = task;
        this.workerId = workerId;
        Service s = task.getService(0);
        this.contact = (String) s.getAttribute("coaster-url");
        if (this.contact == null) {
            this.contact = task.getService(0).getServiceContact().getContact();
        }
        SecurityContext sc = task.getService(0).getSecurityContext();
        if (sc != null) {
            this.cred = (GSSCredential) sc.getCredentials();
        }
    }

    public WorkerShellCommand startCommand(String in) {
        if (in.equals("exit")) {
            dialog.close();
            return null;
        }
        else {
            return startCmd(in, term, new Callback() {
                @Override
                public void replyReceived(Command cmd) {
                    term.commandCompleted();
                }

                @Override
                public void errorReceived(Command cmd, String msg, Exception t) {
                    term.commandFailed(msg, t);
                }
            });
        }
    }

    private WorkerShellCommand startCmd(String cmd, final Terminal term, final Callback cb) {
        try {
            CoasterChannel channel = ChannelManager.getManager()
                .getOrCreateChannel(contact, cred, LocalRequestManager.INSTANCE);
            WorkerShellCommand wsc = new WorkerShellCommand(workerId, cmd) {
                @Override
                public void handleSignal(byte[] data) {
                    term.append(new String(data));
                }
            };
            wsc.executeAsync(channel, cb);
            return wsc;
        }
        catch (ProtocolException e) {
            term.append(e.getMessage());
        }
        catch (Exception e) {
            logger.warn("Cannot execute worker command", e);
            CharArrayWriter caw = new CharArrayWriter();
            e.printStackTrace(new PrintWriter(caw));
            term.append(caw.toString());
        }
        return null;
    }

    public void autoComplete(String in, final Terminal term) {
        startCmd("mls " + in + "*", term, new Callback() {

            @Override
            public void replyReceived(Command cmd) {
                String result = cmd.getInDataAsString(1);
                if (result != null) {
                    String[] r = result.split("\\s+");
                    if (r.length == 1) {
                        term.autoCompleteCallback(r[0]);
                    }
                    else {
                        term.append(join(r));
                    }
                }
            }

            @Override
            public void errorReceived(Command cmd, String msg, Exception t) {
                term.append(msg);
            }
        });
    }
    
    private String join(String[] s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length - 1; i++) {
            sb.append(s[i]);
            sb.append(' ');
        }
        sb.append(s[s.length - 1]);
        return sb.toString();
    }
}
