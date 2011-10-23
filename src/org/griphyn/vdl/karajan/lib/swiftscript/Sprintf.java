package org.griphyn.vdl.karajan.lib.swiftscript;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

/**
    Formatted string generation. <br>
    Example: sprintf("\t%s\n", "hello"); <br>
    Differences from trace(): 
    1) respects \t, \n and \\;
    2) allows for typechecked format specifiers; 
    3) allows for consumption of variables without display (%k) 
    Format specifiers: <br>
      %%: % sign. <br>
      %M: Filename output: waits for close
      %p: Not typechecked, output as in trace(). <br>
      %b: Typechecked boolean output. <br>
      %f: Typechecked float output. <br>
      %i: Typechecked int output. <br>
      %s: Typechecked string output. <br>
      %k: Variable sKipped, no output. <br>
      %q: Array output
 */
public class Sprintf extends VDLFunction {

    private static final Logger logger = 
        Logger.getLogger(Sprintf.class);
    
    static {
        setArguments(Sprintf.class, new Arg[] { Arg.VARGS });
    }
    
    @Override
    protected Object function(VariableStack stack) 
    throws ExecutionException {
        AbstractDataNode[] args = waitForAllVargs(stack);
        
        String msg = format(args); 
        logger.debug("generated: " + msg);
        
        DSHandle result = new RootDataNode(Types.STRING);
        result.setValue(msg);
        return result;
    }

    public static String format(DSHandle[] args) 
    throws ExecutionException {
        if (! (args[0].getType() == Types.STRING))
            throw new ExecutionException
            ("First argument to sprintf() must be a string!"); 

        String spec = (String) args[0].getValue(); 
        logger.debug("spec: " + spec);
        DSHandle[] vars = copyArray(args, 1, args.length-1);
        
        StringBuilder output = new StringBuilder();
        format(spec, vars, output);
        
        return output.toString();
    }

    public static DSHandle[] copyArray(DSHandle[] src, 
                                       int offset, int length)
    {
        DSHandle[] result = new DSHandle[length];
        
        for (int i = 0; i < length; i++)
            result[i] = src[i+offset];
        
        return result;
    }
    
    /** 
       This method can be targeted as a helper function 
       (by @sprintf(), etc.)
     */
    public static void format(String spec, DSHandle[] vars, 
                              StringBuilder output)
    throws ExecutionException
    {
        int i = 0; 
        int arg = 0; 
        while (i < spec.length()) {
            char c = spec.charAt(i);
            if (c == '%') {
                char d = spec.charAt(++i); 
                arg = append(d, arg, vars, output);
            }
            else if (c == '\\') {
                char d = spec.charAt(++i);
                escape(i, spec, d, output); 
            }
            else {
                output.append(c);
            }
            i++;
        }
    }
       
    private static int append(char c, int arg, DSHandle[] vars, 
                       StringBuilder output) 
    throws ExecutionException {
        if (c == '%') {
            output.append('%');
            return arg;
        }
        if (arg >= vars.length) {
            throw new ExecutionException
            ("tracef(): too many specifiers!");
        }
        if (c == 'M') {
            append_M(vars[arg], output);
        }
        else if (c == 'b') { 
            append_b(vars[arg], output);
        }
        else if (c == 'f') {
            append_f(vars[arg], output);
        }
        else if (c == 'i') {
            append_i(vars[arg], output);
        }
        else if (c == 'p') {
            output.append(vars[arg].toString());
        }
        else if (c == 's') {
            append_s(vars[arg], output);
        }
        else if (c == 'q') {
            append_q(vars[arg], output);
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

    private static void append_M(DSHandle arg, StringBuilder output)
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
    
    private static void append_b(DSHandle arg, StringBuilder output) 
    throws ExecutionException {
        if (arg.getType() == Types.BOOLEAN) {
            output.append(arg.getValue());
        }
        else {
            throw new ExecutionException
            ("tracef(): %b requires a boolean!");
        }
    }
    
    private static void append_f(DSHandle arg, StringBuilder output) 
    throws ExecutionException {
        if (arg.getType() == Types.FLOAT) {
            output.append(arg.getValue());
        }
        else {
            throw new ExecutionException
            ("tracef(): %f requires a float!");
        }
    }

    private static void append_i(DSHandle arg, StringBuilder output) 
    throws ExecutionException {
        if (arg.getType() == Types.INT) {
        	Double d = (Double) arg.getValue();
            output.append(new Integer(d.intValue()));
        }
        else {
            throw new ExecutionException
            ("tracef(): %i requires an int!");
        }
    }
    
    private static void append_q(DSHandle arg, StringBuilder output) 
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
                    output.append(handle.getValue());
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
    
    private static void append_s(DSHandle arg, StringBuilder output) 
    throws ExecutionException {
        if (arg.getType() == Types.STRING) {
            output.append(arg.getValue());
        }
        else {
            throw new ExecutionException
            ("tracef(): %s requires a string!");
        }
    }
    
    /**      
     * @param i Only used for error messages
     * @param spec Only used for error messages
     */
    private static void escape(int i, String spec, 
    		                   char c, StringBuilder output) 
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
            ("tracef(): unknown backslash escape sequence! " + 
            		    "(\\" + c + ")\n" + 
            		    "\t in " + spec + " character: " + i);
        }
    }
}
