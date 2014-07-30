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
 * job-status.c
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include <stdlib.h>
#include <time.h>
#include "JobStatus.h"

using namespace Coaster;

using std::string;

void JobStatus::init(CoasterJobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception) {
	this->statusCode = statusCode;
	this->stime = time;
	// always copy strings because the job status' lifetime is weird
	if (message != NULL) {
		this->haveMessage = true;
		this->message.assign(*message);
	}
	else {
		this->haveMessage = false;
		this->message.clear();
	}
	if (exception != NULL) {
		this->exception = exception;
	}
	else {
		this->exception = NULL;
	}
	prev = NULL;
}

JobStatus::JobStatus(CoasterJobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception) {
	init(statusCode, time, message, exception);
}

JobStatus::JobStatus(CoasterJobStatusCode statusCode, const string* message, RemoteCoasterException* exception) {
	 init(statusCode, time(NULL), message, exception);
}

JobStatus::JobStatus(CoasterJobStatusCode statusCode) {
	init(statusCode, time(NULL), NULL, NULL);
}

JobStatus::JobStatus() {
	init(UNSUBMITTED, time(NULL), NULL, NULL);
}

CoasterJobStatusCode JobStatus::getStatusCode() const {
	return statusCode;
}

time_t JobStatus::getTime() {
	return stime;
}

const string* JobStatus::getMessage() const {
	return haveMessage ? &message : NULL;
}

RemoteCoasterException* JobStatus::getException() {
	return exception;
}

void JobStatus::setPreviousStatus(JobStatus* pprev) {
	prev = pprev;
}

const JobStatus* JobStatus::getPreviousStatus() {
	return prev;
}

bool JobStatus::isTerminal() {
	return statusCode == COMPLETED || statusCode == CANCELED || statusCode == FAILED;
}

JobStatus::~JobStatus() {
	if (prev != NULL) {
		delete prev;
	}
}

const char* Coaster::statusCodeToStr(CoasterJobStatusCode code) {
	switch (code) {
		case JobStatus::UNSUBMITTED: return "UNSUBMITTED";
		case JobStatus::SUBMITTING: return "SUBMITTING";
		case JobStatus::SUBMITTED: return "SUBMITTED";
		case JobStatus::ACTIVE: return "ACTIVE";
		case JobStatus::SUSPENDED: return "SUSPENDED";
		case JobStatus::RESUMED: return "RESUMED";
		case JobStatus::FAILED: return "FAILED";
		case JobStatus::CANCELED: return "CANCELED";
		case JobStatus::COMPLETED: return "COMPLETED";
		case JobStatus::STAGE_IN: return "STAGE_IN";
		case JobStatus::STAGE_OUT: return "STAGE_OUT";
		default: return "UNKNWON";
	}
}
