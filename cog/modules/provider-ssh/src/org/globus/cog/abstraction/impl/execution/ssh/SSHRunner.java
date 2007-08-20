// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.ssh;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SSHRunner extends Thread {
	private Ssh ssh;
	private List listeners;
	
	public SSHRunner(Ssh ssh){
		this.ssh = ssh;
	}
	
	public void run(){
		try{
			ssh.execute();
			notifyListeners(SSHTaskStatusListener.COMPLETED, null);
		}
		catch (Exception e){
			notifyListeners(SSHTaskStatusListener.FAILED, e);
		}
	}
	
	public void addListener(SSHTaskStatusListener l){
		if (listeners == null){
			listeners = new LinkedList();
		}
		if (!listeners.contains(l)){
			listeners.add(l);
		}
	}
	
	public void removeListener(SSHTaskStatusListener l){
		if (listeners != null){
			listeners.remove(l);
		}
	}

	public void notifyListeners(int event, Exception e){
		Iterator i = listeners.iterator();
		while (i.hasNext()){
			((SSHTaskStatusListener) i.next()).SSHTaskStatusChanged(event, e);
		}
	}
}
