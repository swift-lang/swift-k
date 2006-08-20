
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.apache.tools.ant.Project;

/**
 * Runs the build in a separate thread.
 */
public class ThreadedBuilder extends Thread{
    Project antProject;
    String target;
    ThreadedBuildListener listener;

    public ThreadedBuilder(Project antProject, String target, ThreadedBuildListener listener){
        this.antProject = antProject;
        this.target = target;
        this.listener = listener;
    }

    public void run(){
        try{
            antProject.executeTarget(target);
            listener.buildFinished(null);
        }
        catch (Exception e){
            e.printStackTrace();
            listener.buildFinished(e);
        }
    }
}
