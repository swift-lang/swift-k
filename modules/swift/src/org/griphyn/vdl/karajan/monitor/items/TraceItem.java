/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;


public class TraceItem extends AbstractStatefulItem {
    private int started, ended;
	
	public TraceItem(String id) {
		super(id);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.TRACE;
	}

    public synchronized void incStarted() {
        started++;
    }

    public synchronized void incEnded() {
        ended++;
    }

    public int getStarted() {
        return started;
    }

    public void setStarted(int started) {
        this.started = started;
    }

    public int getEnded() {
        return ended;
    }

    public void setEnded(int ended) {
        this.ended = ended;
    }

    @Override
    public String toString() {
        return this.getID() + ": " + ended + "/" + started;
    }
}
