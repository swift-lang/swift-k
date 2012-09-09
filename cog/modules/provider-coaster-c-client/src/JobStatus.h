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

using namespace std;

enum JobStatusCode {
	UNSUBMITTED = 0,
	SUBMITTING = 8,
	SUBMITTED = 1,
	ACTIVE = 2,
	SUSPENDED = 3,
	RESUMED = 4,
	FAILED = 5,
	CANCELED = 6,
	COMPLETED = 7,
	STAGE_IN = 16,
	STAGE_OUT = 17,
	UNKNOWN = 9999
};


class JobStatus {
	JobStatusCode statusCode;
	time_t stime;
	string* message;
	string* exception;
	JobStatus* prev;

	public:
		JobStatus(JobStatusCode statusCode, time_t time, const string* message, const string* exception);
		JobStatus(JobStatusCode statusCode, const string* message, const string* exception);
		JobStatus(JobStatusCode statusCode);
		JobStatus();
		virtual ~JobStatus();
		JobStatusCode getStatusCode();
		time_t getTime();
		string* getMessage();
		string* getException();
		const JobStatus* getPreviousStatus();
		void setPreviousStatus(JobStatus* prev);
		static const char* statusCodeToStr(JobStatusCode code);
		bool isTerminal();

		friend ostream& operator<< (ostream& os, JobStatus& s);
		friend ostream& operator<< (ostream& os, JobStatus* s);
};

#endif /* JOB_STATUS_H_ */
