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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 21, 2006
 */
package org.globus.cog.karajan.scheduler.submitQueue;

/**
 * <p>Submit queues are used to implement various throttling parameters.
 * The scheduler builds an array of queues for every task and tasks 
 * bubble up through the queues based on the various throttling parameters.</p>
 * 
 * <p>The submit queues only address the concurrency in the submit() part of a 
 * task's lifecycle. In other words they enforce concurrency limits on the
 * submit() call for tasks.</p>
 * 
 * @author Mihael Hategan
 *
 */
public interface SubmitQueue {
	void queue(NonBlockingSubmit nbs);
    
    void submitCompleted(NonBlockingSubmit nbs, Exception ex);
}
