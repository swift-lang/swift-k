package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Types;

/**
    Formatted file output. <br>
    Example: fprintf("tmp.log", "\t%s\n", "hello"); <br>
    Appends to file.
    @see Tracef, Sprintf
    @author wozniak
 */
public class Fprintf extends VDLFunction {

    private static final Logger logger = 
        Logger.getLogger(Fprintf.class);
    
    static {
        setArguments(Fprintf.class, new Arg[] { Arg.VARGS });
    }

    static ConcurrentHashMap<String, Object> openFiles = 
        new ConcurrentHashMap<String, Object>();
    
    @Override
    protected Object function(VariableStack stack) 
    throws ExecutionException {
        AbstractDataNode[] args = waitForAllVargs(stack);
        
        check(args);
        
        String filename = (String) args[0].getValue();
        String spec = (String) args[1].getValue(); 
        DSHandle[] vars = Sprintf.copyArray(args, 2, args.length-2);
        
        StringBuilder output = new StringBuilder();
        Sprintf.format(spec, vars, output);
        String msg = output.toString();
 
        logger.debug("file: " + filename + " msg: " + msg);        
        write(filename, msg);
        return null;
    }
    
    private static void check(DSHandle[] args) 
    throws ExecutionException {
        if (args.length < 2)
            throw new ExecutionException
            ("fprintf(): requires at least 2 arguments!");
        if (! args[0].getType().equals(Types.STRING))
            throw new ExecutionException
            ("fprintf(): first argument must be a string filename!");
        if (! args[0].getType().equals(Types.STRING))
            throw new ExecutionException
            ("fprintf(): second argument must be a string specifier!");
    }
    
    private static void write(String filename, String msg) 
    throws ExecutionException {
        acquire(filename);
        
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(msg);
            writer.close();
        }
        catch (IOException e) {
            throw new ExecutionException
            ("write(): problem writing to: " + filename, e); 
        }
        
        openFiles.remove(filename);
    }
    
    private static void acquire(String filename) 
    throws ExecutionException {
        int count = 0;
        Object marker = new Object();
        while (openFiles.putIfAbsent(filename, marker) != null && 
                count < 10) {
            try {
                Thread.sleep(count);
            }
            catch (InterruptedException e) 
            {}
            count++;
        }
        if (count == 10)
            throw new ExecutionException
            ("write(): could not acquire: " + filename);
    }
}
