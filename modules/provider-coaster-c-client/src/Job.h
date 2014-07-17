/*
 * job-description.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef JOB_H_
#define JOB_H_

#include "StagingSetEntry.h"
#include "coaster-defs.h"
#include "JobStatus.h"

#include <string>
#include <vector>
#include <map>

namespace Coaster {

/*
  Job represents a single Job that is to be submitted to coaster service.
  The Job object is created and has its parameters set before submission.
  Once submitted, its status is updated to reflect its progress.
 */
class Job {
	private:
		coaster_job_id identity;
		std::string executable;
		/* 
		 * TODO: document expectations about lifetime of strings.
		 * It seems very easy for client code to accidentally
		 * pass in a pointer to a stack-allocated string or a
		 * string with a shorter lifetime than the job.  Would
		 * it work to just store them by value and have zero-length
		 * be equivalent to NULL.  Are zero-length strings meaningful?
		 */
		std::vector<std::string*> arguments;
		std::string* directory;
		std::string* stdinLocation;
		std::string* stdoutLocation;
		std::string* stderrLocation;
		std::string jobManager;

		std::map<std::string, std::string>* env;
		std::map<std::string, std::string>* attributes;
		std::vector<StagingSetEntry>* stageIns;
		std::vector<StagingSetEntry>* stageOuts;
		std::vector<std::string>* cleanups;
		
		std::string *remoteIdentity;

		std::string* stdout;
		std::string* stderr;

		JobStatus* status;
		
		/* Disable default copy constructor */
		Job(const Job&);
		/* Disable default assignment */
		Job& operator=(const Job&);
	public:
		Job(const std::string &executable);
		virtual ~Job();

		const std::string& getExecutable() const;
		
		/*
		  Get the job identity.  The identity is a locally unique integer
		  that is assigned to the job object upon construction
		  and does not change over it's lifetime.
		 */
		coaster_job_id getIdentity() const;
	
		/*
		  Get the remote job identity.  This is a string
		  assigned to the job by the Coasters service.  This
		  will return NULL if we haven't yet found out the
		  remote identity.
		 */
		const std::string* getRemoteIdentity() const;
		void setRemoteIdentity(const std::string& remoteId);

		const std::vector<std::string*>& getArguments();

		/*
		 * Add argument, taking ownership
		 */
		void addArgument(std::string* arg);
		void addArgument(const std::string& arg);
		void addArgument(const char* arg);
		void addArgument(const char* arg, size_t arg_len);

		const std::string* getDirectory() const;
		void setDirectory(std::string& directory);

		const std::string* getStdinLocation() const;
		void setStdinLocation(std::string& loc);

		const std::string* getStdoutLocation() const;
		void setStdoutLocation(std::string& loc);

		const std::string* getStderrLocation() const;
		void setStderrLocation(std::string& loc);

		const std::string &getJobManager() const;
		void setJobManager(std::string jm);
		void setJobManager(const char *jm);
		void setJobManager(const char *jm, size_t jm_len);

		std::map<std::string, std::string>* getEnv();
		const std::string* getEnv(std::string name) const;
		void setEnv(std::string name, std::string value);
		void setEnv(const char *name, size_t name_len,
			    const char *value, size_t value_len);

		std::map<std::string, std::string>* getAttributes();
		const std::string* getAttribute(std::string name);
		void setAttribute(std::string name, std::string value);
		void setAttribute(const char *name, size_t name_len,
			    const char *value, size_t value_len);

		std::vector<StagingSetEntry>* getStageIns();
		void addStageIn(std::string src, std::string dest, CoasterStagingMode mode);

		std::vector<StagingSetEntry>* getStageOuts();
		void addStageOut(std::string src, std::string dest, CoasterStagingMode mode);

		std::vector<std::string>* getCleanups();
		void addCleanup(std::string cleanup);
		void addCleanup(const char *cleanup, size_t cleanup_len);

		const JobStatus* getStatus() const;
		void setStatus(JobStatus* status);

		const std::string* getStdout() const;
		const std::string* getStderr() const;
		
		/*
		 * Return a human-readable string representation
		 * of the job.
		 */
		std::string toString() const;
};

}

#endif /* JOB_DESCRIPTION_H_ */
