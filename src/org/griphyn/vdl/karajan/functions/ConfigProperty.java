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
import org.globus.cog.karajan.util.BoundContact;
import org.apache.log4j.Logger;

public class ConfigProperty extends AbstractFunction {
    public static final Arg NAME = new Arg.Positional("name");
    public static final Arg INSTANCE = new Arg.Optional("instance", Boolean.TRUE);
    public static final Arg HOST = new Arg.Optional("host",null);

    static {
        setArguments(ConfigProperty.class, new Arg[] { NAME, INSTANCE, HOST });
    }

    public static final String INSTANCE_CONFIG_FILE = "vdl:instanceconfigfile";
    public static final String INSTANCE_CONFIG = "vdl:instanceconfig";

    public static final Logger logger = Logger.getLogger(ConfigProperty.class);

    public Object function(VariableStack stack) throws ExecutionException {
        String name = TypeUtil.toString(NAME.getValue(stack));
        boolean instance = TypeUtil.toBoolean(INSTANCE.getValue(stack));
        Object host = HOST.getValue(stack);
        if(logger.isDebugEnabled()) {
            logger.debug("Getting property "+name+" with host "+host);
        }
        if(host!= null) {
            // see if the host has this property defined, and if so
            // get its value
            BoundContact h = (BoundContact)host;
            String prop = (String) h.getProperty(name);
            if(prop != null) {
                logger.debug("Found property "+name+" in BoundContact");
                return prop;
            }
            logger.debug("Could not find property "+name+" in BoundContact");
        }
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
