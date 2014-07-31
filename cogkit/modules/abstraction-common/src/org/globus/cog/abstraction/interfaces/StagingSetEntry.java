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
 * Created on Jan 6, 2010
 */
package org.globus.cog.abstraction.interfaces;

import java.util.EnumSet;

public interface StagingSetEntry {
    
    public enum Mode {
        /**
         * This mode instructs the provider to always stage the file. If the file
         * does not exist, this would result in an error. 
         */
        ALWAYS("always", 1), 
        
        /**
         * The file is only staged if it exists.
         */
        IF_PRESENT("if present", 2), 
        
        /**
         * The file is only staged if an error occurs with the job
         */
        ON_ERROR("on error", 4), 
        
        /**
         * The file is only staged if the job succeeds.
         */
        ON_SUCCESS("on success", 8);
        
        private String str;
        private int id;
        
        private Mode(String str, int id) {
            this.str = str;
            this.id = id;
        }
        
        public String toString() {
            return str;
        }
        
        public int getId() {
            return id;
        }
        
        public static final EnumSet<Mode> ALL = EnumSet.allOf(Mode.class);
        
        static {
            ALL.remove(ALWAYS);
        }
        
        public static EnumSet<Mode> fromId(int id) {
            EnumSet<Mode> mode = EnumSet.noneOf(Mode.class);
            if (id == 0) {
                mode.add(ALWAYS);
            }
            else {
                for (Mode m : ALL) {
                    if ((id & m.getId()) != 0) {
                        mode.add(m);
                    }
                }
            }
            return mode;
        }
        
        public static int getId(EnumSet<Mode> mode) {
            int id = 0;
            for (Mode m : mode) {
                id += m.getId();
            }
            return id;
        }
    }
    
    String getSource();
    
    String getDestination();
    
    /**
     * The mode indicates what circumstances the file should be staged out/in.
     * The modes mostly make sense for stageouts. Not all providers support
     * staging modes and not all mode combinations make sense. The default
     * is (ON_SUCCESS and IF_PRESENT).
     *
     */
    EnumSet<Mode> getMode();
}
