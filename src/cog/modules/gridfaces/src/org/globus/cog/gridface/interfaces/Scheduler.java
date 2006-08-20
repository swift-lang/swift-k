
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/* Need to support the following schedulers
SetScheduler (...QosScheduler.. ) 
SetScheduler (...PriorityScheduler.. ) 
SetScheduler (...SimpleScheduler.. )
SetScheduler (...RoundRobin.. )  

scheduling logic{

    submit and wiat tile executed
    submit and check back at a later time if fails command fails
    
    submit and retry if connection error occurs ?
}

*
**/

public interface Scheduler{
	
	public void execute (String schedulerParameters, String commnad, String arguments);

	public boolean validateSchedulingParameters() throws Exception;

	public void reschedule (String id, String schedulerParameters, String commnad, String arguments);

	public void reschedule (String schedulerParameters, String id);

}