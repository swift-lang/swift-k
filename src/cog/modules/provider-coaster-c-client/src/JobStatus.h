/*
 * job-status.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef JOB_STATUS_H_
#define JOB_STATUS_H_

#include <time.h>
#include <string>
#include <iostream>
#include "RemoteCoasterException.h"
#include "coaster-defs.h"

using namespace std;

class JobStatus {
	private:
		JobStatusCode statusCode;
		time_t stime;
		string* message;
		RemoteCoasterException* exception;
		JobStatus* prev;

		void init(JobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception);

	public:
		JobStatus(JobStatusCode statusCode, time_t time, const string* message, RemoteCoasterException* exception);
		JobStatus(JobStatusCode statusCode, const string* message, RemoteCoasterException* exception);
		JobStatus(JobStatusCode statusCode);
		JobStatus();
		virtual ~JobStatus();
		JobStatusCode getStatusCode() const;
		time_t getTime();
		const string* getMessage() const;
		RemoteCoasterException* getException();
		const JobStatus* getPreviousStatus();
		void setPreviousStatus(JobStatus* prev);
		static const char* statusCodeToStr(JobStatusCode code);
		bool isTerminal();

		template<typename cls> friend cls& operator<< (cls& os, JobStatus& s);
		template<typename cls> friend cls& operator<< (cls& os, JobStatus* s);
};

const char* statusCodeToStr(JobStatusCode code);

template<typename cls> cls& operator<< (cls& os, JobStatus& s) {
	return os << &s;
}

template<typename cls> cls& operator<< (cls& os, JobStatus* s) {
	os << "Status(" << statusCodeToStr(s->getStatusCode());
	if ((s->getMessage() != NULL) && (s->getMessage()->length() != 0)) {
		os << ", msg: " << s->getMessage();
	}
	os << ")";
	return os;
}

#endif /* JOB_STATUS_H_ */
