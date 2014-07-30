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
 * StagingSetEntry.h
 *
 *  Created on: Aug 11, 2012
 *      Author: mike
 */

#ifndef STAGINGSETENTRY_H_
#define STAGINGSETENTRY_H_

#include <string>

#include "coaster-defs.h"

namespace Coaster {

class StagingSetEntry {
	private:
		std::string source;
		std::string destination;
		CoasterStagingMode mode;
	public:
		StagingSetEntry(std::string source, std::string destination, CoasterStagingMode mode);
		std::string getSource();
		std::string getDestination();
		CoasterStagingMode getMode();
		virtual ~StagingSetEntry();

		static const CoasterStagingMode ALWAYS = COASTER_STAGE_ALWAYS;
		static const CoasterStagingMode IF_PRESENT = COASTER_STAGE_IF_PRESENT;
		static const CoasterStagingMode ON_ERROR = COASTER_STAGE_ON_ERROR;
		static const CoasterStagingMode ON_SUCCESS = COASTER_STAGE_ON_SUCCESS;
};

}

#endif /* STAGINGSETENTRY_H_ */
