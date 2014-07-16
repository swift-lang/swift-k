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
