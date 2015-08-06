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
 * Created on Aug 5, 2015
 */
package org.griphyn.vdl.karajan.monitor.items;

public class ApplicationInstance {
    private String attempt;
    private String replicaId;
    private String host;
    private ApplicationState state;
    private long currentStateTime;

    public ApplicationInstance(String attempt, String replicaId) {
        this.attempt = attempt;
        this.replicaId = replicaId;
    }

    public String getAttempt() {
        return attempt;
    }

    public String getReplicaId() {
        return replicaId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setState(ApplicationState state, long time) {
        this.state = state;
        this.currentStateTime = time;
    }
}
