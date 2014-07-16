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
package org.griphyn.vdl.karajan.monitor.monitors;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;

public abstract class AbstractMonitor implements Monitor, SystemStateListener {
	private SystemState state;

	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
		state.addListener(this);
	}

	public void itemUpdated(int updateType, StatefulItem item) {
	}

    @Override
    public boolean supportsOfflineRendering() {
        return false;
    }

    @Override
    public void setOffline(boolean offline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimelineController getTimelineController() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
    }
}
