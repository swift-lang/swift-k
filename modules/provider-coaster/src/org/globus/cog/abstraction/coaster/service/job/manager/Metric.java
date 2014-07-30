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
 * Created on Aug 10, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

public interface Metric {

    Metric NULL_METRIC = new NullMetric();

    double blockSize(int slot, int cslots, double tsum);

    /** Seconds? */
    double getSize(Job j);

    double getSizeLeft(Job j);

    /** Seconds? */
    double desiredSize(Job j);

    double size(int w, int h);

    /**
        Given a size estimate and minimum time length, how many
        worker cores should we allocate?
     */
    int width(int msz, int h);

    public class NullMetric implements Metric {
        public double blockSize(int slot, int cslots, double tsum) {
            return 0;
        }

        public double desiredSize(Job j) {
            return 0;
        }

        public double getSize(Job j) {
            return 0;
        }

        public double getSizeLeft(Job j) {
            return 0;
        }

        public double size(int w, int h) {
            return 0;
        }

        public int width(int msz, int h) {
            return 0;
        }
    }
}
