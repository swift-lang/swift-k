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
 * JobStatusHandler.cpp
 *
 *  Created on: Aug 31, 2012
 *      Author: mike
 */

#include "JobStatusHandler.h"

#include <sstream>

using namespace Coaster;

using std::string;
using std::stringstream;

JobStatusHandler::JobStatusHandler() {
}

JobStatusHandler::~JobStatusHandler() {
}

void JobStatusHandler::requestReceived() {
	string jobId, msg;
	getInDataAsString(0, jobId);
	CoasterJobStatusCode statusCode = (CoasterJobStatusCode) getInDataAsInt(1);
	int exitCode = getInDataAsInt(2);
	getInDataAsString(3, msg);
	long timestamp = getInDataAsLong(4);
	if (statusCode == JobStatus::FAILED && msg.length() == 0) {
		stringstream ss;
		ss << "Job failed with an exit code of " << exitCode;
		msg.assign(ss.str());
	}
	JobStatus* s = new JobStatus(statusCode, timestamp, &msg, NULL);

	getChannel()->getClient()->updateJobStatus(jobId, s);
}
