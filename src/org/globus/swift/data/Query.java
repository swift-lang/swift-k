package org.globus.swift.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;

import org.globus.swift.data.policy.Policy;

public class Query extends FunctionsCollection {

    public static final Arg PA_QUERY = new Arg.Positional("query");

    static {
        setArguments("cdm_query", new Arg[]{ PA_QUERY });
        setArguments("cdm_file", new Arg[]{});
    }

    public String cdm_query(VariableStack stack) throws ExecutionException {
        String file = (String) PA_QUERY.getValue(stack);
        Policy policy = Director.lookup(file);
        return policy.toString();
    }

    public String cdm_file(VariableStack stack) throws ExecutionException {
        String file = "";
        if (Director.policyFile != null)
            file = Director.policyFile.toString();
        return file;
    }
}
