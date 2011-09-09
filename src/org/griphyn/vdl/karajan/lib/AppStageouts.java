/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.LinkedList;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class AppStageouts extends AbstractSequentialWithArguments {

    public static final Arg JOBID = new Arg.Positional("jobid");
    public static final Arg FILES = new Arg.Positional("files");
    public static final Arg DIR = new Arg.Positional("dir");
    public static final Arg STAGING_METHOD = new Arg.Positional("stagingMethod");
    public static final Arg VAR = new Arg.Optional("var", null);
    public static final Arg.Channel STAGEOUT = new Arg.Channel("stageout");

    static {
        setArguments(AppStageouts.class, new Arg[] { JOBID, FILES, DIR,
                STAGING_METHOD, VAR });
    }

    protected void post(VariableStack stack) throws ExecutionException {
        try {
            List files = TypeUtil.toList(FILES.getValue(stack));
            for (Object f : files) { 
                List pv = TypeUtil.toList(f);
                Path p = (Path) pv.get(0);
                DSHandle handle = (DSHandle) pv.get(1);
                ArgUtil.getNamedArguments(stack).add("var", handle.getField(p));
                AbsFile file = new AbsFile(VDLFunction.filename(stack)[0]);
                String protocol = file.getProtocol();
                if (protocol.equals("file")) {
                    protocol = TypeUtil.toString(STAGING_METHOD.getValue(stack));
                }
                String path = file.getDir().equals("") ? file.getName() : file.getDir()
                        + "/" + file.getName();
                String relpath = path.startsWith("/") ? path.substring(1) : path;
                ArgUtil.getChannelReturn(stack, STAGEOUT).append(
                    makeList(TypeUtil.toString(DIR.getValue(stack)) + "/" + relpath,
                        protocol + "://" + file.getHost() + "/" + path));
            }
            super.post(stack);
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
    
    private List<String> makeList(String s1, String s2) {
        List<String> l = new LinkedList<String>();
        l.add(s1);
        l.add(s2);
        return l;
    }
}
