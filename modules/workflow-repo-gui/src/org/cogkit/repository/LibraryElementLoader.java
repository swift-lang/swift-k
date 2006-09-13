package org.cogkit.repository;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import org.cogkit.repository.util.FilteredStream;
import org.cogkit.repository.util.InfoParser;
import org.cogkit.repository.util.NodeInfo;
import org.globus.cog.karajan.KarajanWorkflow;
import org.globus.cog.karajan.Loader;
import org.globus.cog.karajan.SpecificationException;
import org.globus.cog.karajan.workflow.ElementTree;

public class LibraryElementLoader {

    public StringBuffer sb = new StringBuffer(); 
    public String libraryInfo(String libName) throws IOException{
       
        
        PrintStream out = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
        System.setOut(out);
        
        KarajanWorkflow workflow = new KarajanWorkflow();
        try {
            workflow.setSpecification("import(\""+ libName + "\"), info()");
            workflow.start();
            workflow.waitFor();
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SpecificationException e) {
            e.printStackTrace();    
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (workflow.isFailed()) {
            System.err.println("Failed:");
            workflow.getFailure().printStackTrace();
        }
        
        return sb.toString();    
    }
   
    public class FilteredStream extends FilterOutputStream {
        
        public FilteredStream(OutputStream aStream) {
           super(aStream); 
         }

        public void write(byte b[]) throws IOException {
           String aString = new String(b);
           sb.append(aString);
         }

        public void write(byte b[], int off, int len) throws IOException {
           String aString = new String(b , off , len);
           sb.append(aString);
         }
        
        public String getString(){
            return sb.toString();
        }
        
        public void flush() {
            try {
                super.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.flush();
        }
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
       
        LibraryElementLoader libLoader = new LibraryElementLoader();
        String output = "empty";
        PrintStream orig = System.out;
        try {
            output = libLoader.libraryInfo("sys.k");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.setOut(orig);
        
        InfoParser iParse = new InfoParser();
        iParse.setInfo(output);
        NodeInfo[] nodes = iParse.getInfo();    
        
        System.out.println("O/P:");
        for(int i=0; i < nodes.length; i++ ){
            
            System.out.print(nodes[i].getLibName()+ "  " + nodes[i].getNodeName() + " ");
            Vector mandArgs = ((ComponentNodeInfo) nodes[i]).getMandatoryArgs();
            Iterator mandItr = mandArgs.iterator();
            while(mandItr.hasNext()){
                System.out.print(mandItr.next()+ " ");
            }
            
            Vector optArgs = ((ComponentNodeInfo) nodes[i]).getOptionalArgs();
            Iterator optItr = optArgs.iterator();
            while(optItr.hasNext()){
                System.out.print(optItr.next()+"* ");
            }
            System.out.print("\n");
        }
        System.out.println(output);
    }

}
