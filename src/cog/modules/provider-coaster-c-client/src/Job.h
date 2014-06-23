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

using namespace std;

/*
  TODO: document whether a Job can be submitted more than once
 */
class Job {
	private:
		string identity;
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

		string* stdout;
		string* stderr;
		JobStatus* status;
	public:
		Job(const string &executable);
		virtual ~Job();

		const string& getExecutable() const;
		
		/*
		  Get the job identity.  The identity is a unique string
		  that is assigned to the job object upon construction
		  and does not change over it's lifetime.
		 */
		const string& getIdentity() const;

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

		map<string, string>* getAttributes();
		const string* getAttribute(string name);
		void setAttribute(string name, string value);

		vector<StagingSetEntry>* getStageIns();
		void addStageIn(string src, string dest, StagingMode mode);

		vector<StagingSetEntry>* getStageOuts();
		void addStageOut(string src, string dest, StagingMode mode);

		vector<string>* getCleanups();
		void addCleanup(string cleanup);

		const JobStatus* getStatus() const;
		void setStatus(JobStatus* status);
};

#endif /* JOB_DESCRIPTION_H_ */
