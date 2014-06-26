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
#include "RemoteCoasterException.h"
#include "coaster-defs.h"

namespace Coaster {

class JobStatus {
	private:
		CoasterJobStatusCode statusCode;
		time_t stime;
		bool haveMessage;
		std::string message;
		RemoteCoasterException* exception;
		JobStatus* prev;

		void init(CoasterJobStatusCode statusCode, time_t time, const std::string* message, RemoteCoasterException* exception);

		/* Disable default copy constructor */
		JobStatus(const JobStatus&);
		/* Disable default assignment */
		JobStatus& operator=(const JobStatus&);
	public:
		JobStatus(CoasterJobStatusCode statusCode, time_t time, const std::string* message, RemoteCoasterException* exception);
		JobStatus(CoasterJobStatusCode statusCode, const std::string* message, RemoteCoasterException* exception);
		JobStatus(CoasterJobStatusCode statusCode);
		JobStatus();
		virtual ~JobStatus();
		CoasterJobStatusCode getStatusCode() const;
		time_t getTime();
		const std::string* getMessage() const;
		RemoteCoasterException* getException();
		const JobStatus* getPreviousStatus();
		void setPreviousStatus(JobStatus* prev);
		static const char* statusCodeToStr(CoasterJobStatusCode code);
		bool isTerminal();

		template<typename cls> friend cls& operator<< (cls& os, JobStatus& s);
		template<typename cls> friend cls& operator<< (cls& os, JobStatus* s);

                static const CoasterJobStatusCode UNSUBMITTED = COASTER_STATUS_UNSUBMITTED;
                static const CoasterJobStatusCode SUBMITTING = COASTER_STATUS_SUBMITTING;
                static const CoasterJobStatusCode SUBMITTED = COASTER_STATUS_SUBMITTED;
                static const CoasterJobStatusCode ACTIVE = COASTER_STATUS_ACTIVE;
	        static const CoasterJobStatusCode SUSPENDED = COASTER_STATUS_SUSPENDED;
                static const CoasterJobStatusCode RESUMED = COASTER_STATUS_RESUMED;
                static const CoasterJobStatusCode FAILED = COASTER_STATUS_FAILED;
                static const CoasterJobStatusCode CANCELED = COASTER_STATUS_CANCELED;
                static const CoasterJobStatusCode COMPLETED = COASTER_STATUS_COMPLETED;
                static const CoasterJobStatusCode STAGE_IN = COASTER_STATUS_STAGE_IN;
                static const CoasterJobStatusCode STAGE_OUT = COASTER_STATUS_STAGE_OUT;
                static const CoasterJobStatusCode UNKNOWN = COASTER_STATUS_UNKNOWN;
};

const char* statusCodeToStr(CoasterJobStatusCode code);

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

}

#endif /* JOB_STATUS_H_ */
