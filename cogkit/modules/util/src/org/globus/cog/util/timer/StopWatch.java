/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

// Comments: not threadsafe, may cost time. if we assume i is unique
// and its calls or not messed up through threads it shoudl be ok.

// javadoc is incomplete and needs to be fixed

// this class does not use log4j this needs to be changed

package org.globus.cog.util.timer;

import org.apache.log4j.Logger;

/**
   A stopwatch to measure time.
*/ 
public class  StopWatch {
    private static Logger logger = Logger.getLogger(StopWatch.class);    

    public static long [] timer;
    public static String [] timerName;
    private long[] startTime; 	
    private long[] stopTime;
    private long[] pausedTime;
    private long[] storedTime;
    private boolean[] running;
    private int n;

    /**
     * Creates a new <code>StopWatch</code> instance.
     *
     * @param n an <code>int</code> value
     */
    public StopWatch(int n) {
	init(n);
    }
    
    /**
     * The <code>init</code> method creates n timers each timer has a
     * default name "Time i" where i is a number between 1 and n.
     *
     * @param n an <code>int</code> value
     */
    public void init (int n) { 
	timer = new long[n];
	startTime = new long[n];
	stopTime = new long[n];
	storedTime = new long[n];
	running = new boolean[n];
	timerName = new String[n];
	this.n = n;
    }
    

    /**
     * The <code>reset</code> method resets the timer.
     *
     * @param i an <code>int</code> value
     */
    public void reset (int i) {
	if (running[i]) {
	    start(i);
	}
	else {
	    logger.warn("Timer " + i + 
			" (" + timerName[i] + 
		        ") is not running.");
	}
    }
    
    /**
     * The <code>set</code> method sets the name of the ith timer.
     *
     * @param i an <code>int</code> value
     * @param name a <code>String</code> value
     */
    public void set (int i, String name) {
	if (i > n) {
	    logger.warn("This time number "+ i + " is too big, only " + n 
			+ " timers allowed");
	} else {
	    timerName[i] = name;	
	} 
    }
    
    /**
     * The <code>getIndex</code> method gets the index of the
     * timer with the given name
     *
     * @param name a <code>String</code> value
     * @return an <code>int</code> value
     */
    public int getIndex (String name) {
	int index = -1;
	for(int i=0; i < size(); i++){
	    if(timerName[i].equals(name)) index = i;
	}
	return index;
    }
    
    /**
     * The <code>get</code> method gets the value of the timer
     * with the given name check.
     *
     * @param i an <code>int</code> value
     * @return a <code>long</code> value
     */
    public long get (int i) {
	long elapsedTime;
	if (running[i]) {
	    elapsedTime = (System.currentTimeMillis()-startTime[i]);
	}
	else{
	    elapsedTime = (stopTime[i] - startTime[i]);
	}
	return elapsedTime;
    }
    
    /**
     * The <code>add</code> method add the time to the timer i.
     *
     * @param i an <code>int</code> value
     * @param time a <code>long</code> value
     */
    public void add (int i, long time) {
	startTime[i] = startTime[i] - time;
    }
    
    /**
     * The <code>add</code> method add the time to timer "name".
     *
     * @param name a <code>String</code> value
     * @param time a <code>long</code> value
     */
    public void add (String name, long time) {
	int i = getIndex(name);
	add(i, time);	
    }

    
    /**
     * The  <code>size</code> method returns the number of timers.
     *
     * @return an <code>int</code> value
     */
    public int size () {
	return n;
    }
    
    /**
     * The <code>start</code> method starts the ith timer.
     *
     * @param i an <code>int</code> value
     */
    public void start (int i) {
	startTime[i] = System.currentTimeMillis();
	running[i] = true;
    }
    
    /**
     * The <code>stop</code> method stops the ith timer.
     *
     * @param i an <code>int</code> value
     */
    public void stop (int i){
	stopTime[i] = System.currentTimeMillis();
	running[i] = false;
    }
    

    /**
     * The <code>pause</code> method pause the ith timer.
     *
     * @param i an <code>int</code> value
     */
    public void pause (int i) {
	// this will give perfomance problems all multiplications
	// should be done last i suggest to reomve the * 1000, alos
	// does thes need to be i suggest to keep stored time in
	// miliseconds and only at print time changing it as with the
	// other timers
	storedTime[i] = get(i) * 1000;
	running[i] = false;
    }
    

    /**
     * The <code>resume</code> method resumes the ith timer.
     *
     * @param i an <code>int</code> value
     */
    public void resume (int i) {
	start(i);
	add(i, storedTime[i]);
	running[i]= true;
    }
    

    /**
     * The <code>printTimer</code> method prints the ith timer result
     * in seconds.
     *
     * @param i an <code>int</code> value
     */
    public void printTimer (int i) {
	System.out.println("Timer " + timerName[i]+ "[" +i+ "]  : " + get(i)+"ms");
    }
    
    /**
     * The <code>printTimer</code> method prints the ith timer result.
     *
     * @param name a <code>String</code> value
     */
    public void printTimer (String name) {
	int i = getIndex(name);
	if(i > -1){
	    printTimer(i);
	}
	else{
	    logger.warn("Timer '" + timerName + "' is invalid");
	}
    }

    /**
     * Describe <code>printTimers</code> method prints all timer results.
     *
     */
    public void printTimers() {
	String status;
	for(int i=0; i < size(); i++){
	    if (running[i]) {
		status = "running";
	    } else {
		status = "stopped/paused";
		System.out.println("Timer " + timerName[i] + "[" +i+ "]  : " +
				   get(i) + "ms. Status :" + status);
	    }
	}
    
    }

}
