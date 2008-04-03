/*
 * Created on Dec 6, 2006
 */
package org.griphyn.vdl.karajan.functions;

import java.io.IOException;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;
import org.griphyn.vdl.util.VDL2Config;

public class ConfigProperty extends AbstractFunction {
    public static final Arg NAME = new Arg.Positional("name");
    public static final Arg INSTANCE = new Arg.Optional("instance", Boolean.TRUE);

    static {
        setArguments(ConfigProperty.class, new Arg[] { NAME, INSTANCE });
    }

    public static final String INSTANCE_CONFIG_FILE = "vdl:instanceconfigfile";
    public static final String INSTANCE_CONFIG = "vdl:instanceconfig";

    public Object function(VariableStack stack) throws ExecutionException {
        String name = TypeUtil.toString(NAME.getValue(stack));
        boolean instance = TypeUtil.toBoolean(INSTANCE.getValue(stack));
        return getProperty(name, instance, stack);
    }

    public static String getProperty(String name, VariableStack stack) throws ExecutionException {
        return getProperty(name, true, stack);
    }

    public static String getProperty(String name, boolean instance, VariableStack stack) throws ExecutionException {
        try {
            VDL2Config conf;
            String prop;
            if (!instance) {
                conf = VDL2Config.getConfig();
                prop = conf.getProperty(name);
            }
            else {
                synchronized (stack.firstFrame()) {
                    conf = (VDL2Config) stack.getGlobal(INSTANCE_CONFIG);
                    if (conf == null) {
                        String confFile = (String) stack.getGlobal(INSTANCE_CONFIG_FILE);
                        if (confFile == null) {
                            conf = VDL2Config.getConfig();
                        }
                        else {
                            conf = VDL2Config.getConfig(confFile);
                        }
                        stack.setGlobal(INSTANCE_CONFIG, conf);
                    }
                    prop = conf.getProperty(name);
                }
            }
            if (prop == null) {
                throw new ExecutionException("Swift config property \"" + name + "\" not found in "
                        + conf);
            }
            else {
                return prop;
            }
        }
        catch (IOException e) {
            throw new ExecutionException("Failed to load Swift configuration", e);
        }
    }
}
