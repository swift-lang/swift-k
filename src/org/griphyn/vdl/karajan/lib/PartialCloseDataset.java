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


package org.griphyn.vdl.karajan.lib;

import org.griphyn.vdl.mapping.nodes.PartialCloseable;


public class PartialCloseDataset extends PartialCleanOrClose<PartialCloseable> {

    @Override
    protected void doWhatNeedsToBeDone(PartialCloseable var, int count) {
        var.updateWriteRefCount(-count);
    }

    @Override
    protected boolean ignoreStaticRefs() {
        // we don't clean static refs because there aren't many of them,
        // but they must be closed properly
        return false;
    }
}
