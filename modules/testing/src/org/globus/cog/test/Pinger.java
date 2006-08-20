
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import java.net.*;

public class Pinger extends Thread{
    private volatile boolean finished;
    private volatile boolean timerExpired;
    private String machine;
    private int port;
    
    
    public Pinger(String machine, int port){
	finished = false;
	this.machine = machine;
	this.port = port;
    }
    
    
    
    public void run(){
	try{
	    Socket sock = new Socket(machine, port);
	}
	catch(Exception e){
	}
	finished = true;
    }
    
    public boolean finished(){
	return finished;
    }
    
    public static boolean ping(String machine, int port, int timeout){
	int ms = 0;
	Pinger myPinger = new Pinger(machine, port);
	myPinger.start();
	while ((ms < timeout) && (!myPinger.finished())) { 
	    try{
		Thread.sleep(100);
	    }
	    catch(InterruptedException e){
	    }
	    ms += 100;
	}
	
	if (myPinger.finished()){
	    return true;
	}
	else{
	    return false;
	}
    }
    
    public static boolean ping(String machine, int port){
	return ping(machine, port, 20000);
    }

    public static boolean ping(String machine){
	int sep = machine.indexOf(':');
	if(sep != -1){
	    return ping(machine.substring(0, sep), Integer.parseInt(machine.substring(sep+1)));
	}
	return ping(machine, 2119);
    }
}
