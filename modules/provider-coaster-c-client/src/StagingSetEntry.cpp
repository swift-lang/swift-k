/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2012-2014 University of Chicago
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


/*
 * StagingSetEntry.cpp
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#include "StagingSetEntry.h"

using namespace Coaster;

using std::string;

StagingSetEntry::StagingSetEntry(string psource, string pdestination, CoasterStagingMode pmode) {
	source = psource;
	destination = pdestination;
	mode = pmode;
}

string StagingSetEntry::getSource() {
	return source;
}

string StagingSetEntry::getDestination() {
	return destination;
}

CoasterStagingMode StagingSetEntry::getMode() {
	return mode;
}

StagingSetEntry::~StagingSetEntry() {
}
