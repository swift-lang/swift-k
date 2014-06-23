/*
 * job-description.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef JOB_H_
#define JOB_H_

#include "StagingSetEntry.h"
#include "JobStatus.h"
#include <string>
#include <vector>
#include <map>
#include <stdint.h>

using namespace std;

// 64-bit job ids should be sufficient to be unique
typedef int64_t job_id_t;

/*
  Job represents a single Job that is to be submitted to coasters.
  The Job object is created and has its parameters set before submission.
  Once submitted, its status is updated to reflect its progress.
 */
class Job {
	private:
		job_id_t identity;
		string executable;
		/* 
		 * TODO: document expectations about lifetime of strings.
		 * It seems very easy for client code to accidentally
		 * pass in a pointer to a stack-allocated string or a
		 * string with a shorter lifetime than the job.  Would
		 * it work to just store them by value and have zero-length
		 * be equivalent to NULL.  Are zero-length strings meaningful?
		 */
		vector<string*>* arguments;
		string* directory;
		string* stdinLocation;
		string* stdoutLocation;
		string* stderrLocation;
		string jobManager;

		map<string, string>* env;
		map<string, string>* attributes;
		vector<StagingSetEntry>* stageIns;
		vector<StagingSetEntry>* stageOuts;
		vector<string>* cleanups;
		
		string *remoteIdentity;

		string* stdout;
		string* stderr;

		JobStatus* status;
	public:
		Job(const string &executable);
		virtual ~Job();

		const string& getExecutable() const;
		
		/*
		  Get the job identity.  The identity is a locally unique integer
		  that is assigned to the job object upon construction
		  and does not change over it's lifetime.
		 */
		job_id_t getIdentity() const;
	
		/*
		  Get the remote job identity.  This is a string
		  assigned to the job by the Coasters service.  This
		  will return NULL if we haven't yet found out the
		  remote identity.
		 */
		const string* getRemoteIdentity() const;
		void setRemoteIdentity(const string& remoteId);

		vector<string*>* getArguments();
		void addArgument(string& arg);
		void addArgument(const char* arg);
		void addArgument(const char* arg, size_t arg_len);

		const string* getDirectory() const;
		void setDirectory(string& directory);

		const string* getStdinLocation() const;
		void setStdinLocation(string& loc);

		const string* getStdoutLocation() const;
		void setStdoutLocation(string& loc);

		const string* getStderrLocation() const;
		void setStderrLocation(string& loc);

		const string &getJobManager() const;
		void setJobManager(string jm);
		void setJobManager(const char *jm);
		void setJobManager(const char *jm, size_t jm_len);

		map<string, string>* getEnv();
		const string* getEnv(string name) const;
		void setEnv(string name, string value);
		void setEnv(const char *name, size_t name_len,
			    const char *value, size_t value_len);

		map<string, string>* getAttributes();
		const string* getAttribute(string name);
		void setAttribute(string name, string value);
		void setAttribute(const char *name, size_t name_len,
			    const char *value, size_t value_len);

		vector<StagingSetEntry>* getStageIns();
		void addStageIn(string src, string dest, StagingMode mode);

		vector<StagingSetEntry>* getStageOuts();
		void addStageOut(string src, string dest, StagingMode mode);

		vector<string>* getCleanups();
		void addCleanup(string cleanup);

		const JobStatus* getStatus() const;
		void setStatus(JobStatus* status);

		const string* getStdout() const;
		const string* getStderr() const;
};

#endif /* JOB_DESCRIPTION_H_ */
