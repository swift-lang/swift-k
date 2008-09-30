//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class GetFileCommand extends Command {
    public static final String NAME = "GET";
    private long len = -1;
    private FileOutputStream fos;
    private String local;
    private ProgressMonitor pm;

    public GetFileCommand(String remote, String local, ProgressMonitor pm) throws FileNotFoundException {
        super(NAME);
        addOutData(remote);
        this.local = local;
        this.pm = pm;
        fos = new FileOutputStream(local);
    }

    protected void addInData(byte[] data) {
        if (len == -1) {
            len = unpackLong(data);
        }
        else {
            try {
                fos.write(data);
            }
            catch (IOException e) {
                errorReceived(e.getMessage(), e);
            }
        }
    }

    public void receiveCompleted() {
        super.receiveCompleted();
        try {
            fos.close();
        }
        catch (IOException e) {
            errorReceived(e.getMessage(), e);
        }
    }
}
