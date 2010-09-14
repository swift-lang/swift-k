package org.griphyn.vdl.karajan.lib.swiftscript;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.SwiftArg;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Types;

/**
    Formatted trace output. <br>
    Example: tracef("\t%s\n", "hello"); <br>
    Differences from trace(): 
    1) respects \t, \n and \\;
    2) allows for typechecked format specifiers; 
    3) allows for consumption of variables without display (%k); 
    4) does not impose any formatting (commas, etc.).  <br><br>
    Format specifiers: <br>
      %%: % sign. <br>
      %M: Filename output: waits for close
      %p: Not typechecked, output as in trace(). <br>
      %f: Typechecked float output. <br>
      %i: Typechecked int output. <br>
      %s: Typechecked string output. <br>
      %k: Variable sKipped, no output. <br>
      %q: Array output
 */
public class Tracef extends VDLFunction {

    private static final Logger logger = 
        Logger.getLogger(Tracef.class);
    
    static {
        setArguments(Tracef.class, new Arg[] { Arg.VARGS });
    }
    
    @Override
    protected Object function(VariableStack stack) 
    throws ExecutionException {
        DSHandle[] args = SwiftArg.VARGS.asDSHandleArray(stack);

        for (int i = 0; i < args.length; i++) {
            DSHandle handle = args[i];
            VDLFunction.waitFor(stack, handle);
        }
        String msg = format(args); 
        logger.info(msg);
        System.out.print(msg);
        return null;
    }

    private String format(DSHandle[] args) 
    throws ExecutionException {
        if (! (args[0].getType() == Types.STRING))
            throw new ExecutionException
            ("First argument to tracef() must be a string!"); 

        String spec = args[0].toString(); 
        StringBuffer output = new StringBuffer(); 
        int i = 0; 
        int arg = 1; 
        while (i < spec.length()) {
            char c = spec.charAt(i);
            if (c == '%') {
                char d = spec.charAt(++i); 
                arg = append(d, arg, args, output);
            }
            else if (c == '\\') {
                char d = spec.charAt(++i);
                escape(d, output); 
            }
            else {
                output.append(c);
            }
            i++;
        }
        String result = output.toString(); 
        return result; 
    }

    private int append(char c, int arg, DSHandle[] args, 
                       StringBuffer output) 
    throws ExecutionException {
        if (c == '%') {
            output.append('%');
            return arg;
        }
        if (arg >= args.length) {
            throw new ExecutionException
            ("tracef(): too many specifiers!");
        }
        if (c == 'M') {
            append_M(args[arg], output);
        }
        else if (c == 'f') {
            append_f(args[arg], output);
        }
        else if (c == 'i') {
            append_i(args[arg], output);
        }
        else if (c == 'p') {
            output.append(args[arg].toString());
        }
        else if (c == 's') {
            append_s(args[arg], output);
        }
        else if (c == 'q') {
            append_q(args[arg], output);
        }
        else if (c == 'k') {
            ;
        }
        else {
            throw new ExecutionException
            ("tracef(): Unknown format: %" + c);
        }
        return arg+1;
    }

    private void append_M(DSHandle arg, StringBuffer output) 
    throws ExecutionException {
        try {
            synchronized (arg.getRoot()) { 
                String[] names = VDLFunction.filename(arg);
                if (names.length > 1)
                    output.append(names);
                else 
                    output.append(names[0]);
            }
        }
        catch (Exception e) { 
            throw new ExecutionException
            ("tracef(%M): Could not lookup: " + arg); 
        }
    }
    
    private void append_f(DSHandle arg, StringBuffer output) 
    throws ExecutionException {
        if (arg.getType() == Types.FLOAT) {
            output.append(arg).toString();
        }
        else {
            throw new ExecutionException
            ("tracef(): %f requires a float!");
        }
    }

    private void append_i(DSHandle arg, StringBuffer output) 
    throws ExecutionException {
        if (arg.getType() == Types.INT) {
            output.append(arg).toString();
        }
        else {
            throw new ExecutionException
            ("tracef(): %i requires an int!");
        }
    }
    
    private void append_q(DSHandle arg, StringBuffer output) 
    throws ExecutionException {
        if (arg instanceof ArrayDataNode) {
            ArrayDataNode node = (ArrayDataNode) arg;
            output.append("[");
            try {
                int size = node.size();
                for (int i = 0; i < size; i++) {
                    String entry = ""+i; 
                    DSHandle handle = 
                        node.getField(Path.parse(entry));
                    output.append(handle);
                    if (i < size-1)
                        output.append(",");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new ExecutionException
                ("trace(%q): Could not get children of: " + arg);
            }
            output.append("]");
        }
        else {
            throw new ExecutionException
            ("tracef(): %q requires an array!");
        }        
    }
    
    private void append_s(DSHandle arg, StringBuffer output) 
    throws ExecutionException {
        if (arg.getType() == Types.STRING) {
            output.append(arg).toString();
        }
        else {
            throw new ExecutionException
            ("tracef(): %s requires a string!");
        }
    }
    
    private void escape(char c, StringBuffer output) 
    throws ExecutionException {
        if (c == '\\') {
            output.append('\\');
        }
        else if (c == 'n') {
            output.append('\n');
        }
        else if (c == 't') {
            output.append('\t');
        }
        else {
            throw new ExecutionException
            ("tracef(): unknown backslash escape sequence!");
        }
    }


}
