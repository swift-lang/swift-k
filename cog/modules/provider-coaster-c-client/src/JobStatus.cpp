/*
 * job-status.c
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include <stdlib.h>
#include <time.h>
#include "JobStatus.h"

JobStatus::JobStatus(JobStatusCode pstatusCode, time_t ptime, const string* pmessage, const string* pexception) {
	statusCode = pstatusCode;
	stime = ptime;
	// always copy strings because the job status' lifetime is weird
	if (pmessage != NULL) {
		message = new string(*pmessage);
	}
	else {
		message = NULL;
	}
	if (pexception != NULL) {
		exception = new string(*pexception);
	}
	else {
		exception = NULL;
	}
}

JobStatus::JobStatus(JobStatusCode pstatusCode, const string* pmessage, const string* pexception) {
	JobStatus(pstatusCode, time(NULL), pmessage, pexception);
}

JobStatus::JobStatus(JobStatusCode pstatusCode) {
	JobStatus(pstatusCode, NULL, NULL);
}

JobStatus::JobStatus() {
	JobStatus(UNSUBMITTED);
}

JobStatusCode JobStatus::getStatusCode() {
	return statusCode;
}

time_t JobStatus::getTime() {
	return stime;
}

string* JobStatus::getMessage() {
	return message;
}

string* JobStatus::getException() {
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

static const char* statusCodeToStr(JobStatusCode code) {
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

ostream& operator<< (ostream& os, JobStatus& s) {
	return os << &s;
}

ostream& operator<< (ostream& os, JobStatus* s) {
	os << "Status(" << statusCodeToStr(s->getStatusCode());
	if (s->getMessage()->length() != 0) {
		os << ", msg: " << s->getMessage();
	}
	os << ")";
	return os;
}

