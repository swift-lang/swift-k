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
 * Created on Dec 6, 2006
 */
package org.griphyn.vdl.karajan.functions;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.VDL2ErrorTranslator;

public class ProcessBulkErrors extends AbstractFunction {
	public static final Logger logger = Logger.getLogger(ProcessBulkErrors.class);
	
	private ArgRef<String> message;
	private ArgRef<List<ExecutionException>> errors;
	private ArgRef<Boolean> onStdout;
	
	private ChannelRef<Object> cr_stdout, cr_stderr;

	@Override
    protected Signature getSignature() {
        return new Signature(params("message", "errors", optional("onStdout", false)), 
            returns(channel("stdout", DYNAMIC), channel("stderr", DYNAMIC)));
    }

	@Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
	    returnDynamic(scope);
        return super.compileBody(w, argScope, scope);
    }



    @Override
	public Object function(Stack stack) {
		String message = this.message.getValue(stack);
		boolean onStdout = this.onStdout.getValue(stack);
		Collection<ExecutionException> l = this.errors.getValue(stack);

		VDL2ErrorTranslator translator = VDL2ErrorTranslator.getDefault();

		Map<String, Integer> count = new HashMap<String, Integer>();
		for (ExecutionException ex : l) {
		    if (ex == null) {
		        continue;
		    }
			if (ex.getCause() instanceof ConcurrentModificationException) {
				ex.printStackTrace();
			}
			if (logger.isDebugEnabled()) {
				logger.debug(ex);
			}
			String msg = getMessageChain(ex);
			String tmsg = translator.translate(msg);
			if (tmsg == null) {
				if (msg != null && msg.startsWith("VDL2: ")) {
					tmsg = ex.getMessage().substring(6);
				}
				else {
					tmsg = msg;
				}
			}
			tmsg = tmsg.trim();
			if (count.containsKey(tmsg)) {
				Integer j = count.get(tmsg);
				count.put(tmsg, new Integer(j.intValue() + 1));
			}
			else {
				count.put(tmsg, new Integer(1));
			}
		}
		ChannelRef<Object> channel = onStdout ? cr_stdout : cr_stderr;
		Channel<Object> r = channel.get(stack);
		if (count.size() != 0) {
			r.add(message + "\n");
			int k = 1;
			for (Map.Entry<String, Integer> e : count.entrySet()) {
				Integer j = e.getValue();
				if (j.intValue() == 1) {
					r.add(k + ". " + e.getKey() + "\n");
				}
				else {
					r.add(k + ". " + e.getKey() + " (" + j.intValue() + " times)\n");
				}
				k++;
			}
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public static String getMessageChain(Throwable e) {
	    Throwable orig = e;
		StringBuffer sb = new StringBuffer();
		String prev = null;
		String lastmsg = null;
		boolean first = true;
		while (e != null) {
			String msg;
			if (e instanceof NullPointerException || e instanceof ClassCastException) {
			    CharArrayWriter caw = new CharArrayWriter();
			    e.printStackTrace(new PrintWriter(caw));
			    msg = caw.toString();
			}
			else if (e instanceof ExecutionException) {
			    msg = getMsgAndTrace((ExecutionException) e);
			}
			else {
			    msg = e.getMessage();
			    if (msg != null) {
			        lastmsg = msg;
			    }
			}
			if (msg != null && (prev == null || prev.indexOf(msg) == -1)) {
			    if (!first) {
			        sb.append("\nCaused by:\n\t");
			    }
			    else {
			        first = false;
			    }
			    sb.append(msg);
			    lastmsg = msg;
			    prev = msg;
			}
			e = e.getCause();
		}
		return sb.toString();
	}

    private static String getMsgAndTrace(ExecutionException e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        sb.append('\n');
        if (e.getTrace() != null) {
            for (Node n : e.getTrace()) {
                sb.append("\t");
                demangle(sb, n.getTextualName());
                String fn = n.getFileName();
                if (fn != null) {
                    fn = fn.substring(1 + fn.lastIndexOf('/'));
                    sb.append(" @ ");
                    sb.append(fn);
        
                    if (n.getLine() != 0) {
                        sb.append(", line: ");
                        sb.append(n.getLine());
                    }
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static void demangle(StringBuilder sb, String name) {
        boolean seenParams = false;
        boolean inArray = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            switch (c) {
                case '$':
                    if (i == name.length() - 1) {
                        if (inArray) {
                            sb.append("]");
                        }
                        sb.append(")");
                    }
                    else if (seenParams) {
                        if (inArray) {
                            sb.append("]");
                            inArray = false;
                        }
                        sb.append(", ");
                    }
                    else {
                        sb.append("(");
                        seenParams = true;
                    }
                    break;
                case '#':
                    sb.append("[");
                    inArray = true;
                    break;
                default:
                    sb.append(c);
            }
        }
    }
}
