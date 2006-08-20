/*
 * 
 */
package org.globus.cog.gridshell.tasks.test;
import org.globus.cog.gridshell.tasks.AbstractTask;
import org.globus.cog.gridshell.tasks.ExecTask;

/**
 * 
 */
public class ExecTest {
    public static void main(String[] args) {
        try {
            AbstractTask exec = new ExecTask("gt2","wiggum.mcs.anl.gov",-1,"/bin/ls","-l");
            exec.initTask();
            exec.submitAndWait();
            System.out.println(exec.getResult());
        } catch(Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
