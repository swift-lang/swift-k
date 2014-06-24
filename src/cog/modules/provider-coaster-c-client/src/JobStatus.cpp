/*
 * job-status.c
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include <stdlib.h>
#include <time.h>
#include "JobStatus.h"

void JobStatus::init(JobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception) {
	this->statusCode = statusCode;
	this->stime = time;
	// always copy strings because the job status' lifetime is weird
	if (message != NULL) {
		this->message = new string(*message);
	}
	else {
		this->message = NULL;
	}
	if (exception != NULL) {
		this->exception = exception;
	}
	else {
		this->exception = NULL;
	}
	prev = NULL;
}

JobStatus::JobStatus(JobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception) {
	init(statusCode, time, message, exception);
}

JobStatus::JobStatus(JobStatusCode statusCode, const string* message, RemoteCoasterException* exception) {
	 init(statusCode, time(NULL), message, exception);
}

JobStatus::JobStatus(JobStatusCode statusCode) {
	init(statusCode, time(NULL), NULL, NULL);
}

JobStatus::JobStatus() {
	init(UNSUBMITTED, time(NULL), NULL, NULL);
}

JobStatusCode JobStatus::getStatusCode() const {
	return statusCode;
}

time_t JobStatus::getTime() {
	return stime;
}

const string* JobStatus::getMessage() const {
	return message;
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
	if (message != NULL) {
		//delete message;
	}
}

const char* statusCodeToStr(JobStatusCode code) {
	switch (code) {
		case UNSUBMITTED: return "UNSUBMITTED";
		case SUBMITTING: return "SUBMITTING";
		case SUBMITTED: return "SUBMITTED";
		case ACTIVE: return "ACTIVE";
		case SUSPENDED: return "SUSPENDED";
		case RESUMED: return "RESUMED";
		case FAILED: return "FAILED";
		case CANCELED: return "CANCELED";
		case COMPLETED: return "COMPLETED";
		case STAGE_IN: return "STAGE_IN";
		case STAGE_OUT: return "STAGE_OUT";
		default: return "UNKNWON";
	}
}
