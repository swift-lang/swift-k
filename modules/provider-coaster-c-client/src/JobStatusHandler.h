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
 * JobStatusHandler.h
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#ifndef JOBSTATUSHANDLER_H_
#define JOBSTATUSHANDLER_H_

#include "Handler.h"
#include "JobStatus.h"

namespace Coaster {

class JobStatusHandler: public Handler {
	public:
		JobStatusHandler();
		virtual ~JobStatusHandler();
		virtual void requestReceived();
};

}

#endif /* JOBSTATUSHANDLER_H_ */
