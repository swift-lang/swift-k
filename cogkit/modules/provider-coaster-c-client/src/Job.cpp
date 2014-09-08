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


#include "Job.h"
#include <sstream>
#include <cstring>

using namespace Coaster;

using std::map;
using std::pair;
using std::string;
using std::stringstream;
using std::vector;

static coaster_job_id seq = 0;

Job::Job(const string &pexecutable) {
	executable = pexecutable;
	identity = seq++;

	directory = NULL;
	stdinLocation = NULL;
	stdoutLocation = NULL;
	stderrLocation = NULL;
	jobManager = string("");

	env = NULL;
	attributes = NULL;
	stageIns = NULL;
	stageOuts = NULL;
	cleanups = NULL;

	stdout = NULL;
	stderr = NULL;

	status = NULL;
}

coaster_job_id Job::getIdentity() const {
	return identity;
}

const vector<string*>& Job::getArguments() {
	return arguments;
}

void Job::addArgument(string* arg) {
	arguments.push_back(arg);
}

const string& Job::getExecutable() const {
	return executable;
}

void Job::addArgument(const string& arg) {
	addArgument(new string(arg));
}

void Job::addArgument(const char* arg) {
	addArgument(new string(arg));
}

void Job::addArgument(const char* arg, size_t arg_len) {
	addArgument(new string(arg, arg_len));
}

const string* Job::getDirectory() const {
	return directory;
}

void Job::setDirectory(string& pdirectory) {
	directory = &pdirectory;
}

const string* Job::getStdinLocation() const {
	return stdinLocation;
}

void Job::setStdinLocation(string& loc) {
	stdinLocation = &loc;
}

const string* Job::getStdoutLocation() const {
	return stdoutLocation;
}

void Job::setStdoutLocation(string& loc) {
	stdoutLocation = &loc;
}

const string* Job::getStderrLocation() const {
	return stderrLocation;
}

void Job::setStderrLocation(string& loc) {
	stderrLocation = &loc;
}

const string& Job::getJobManager() const {
	return jobManager;
}

void Job::setJobManager(string jm) {
    // cout << "Job.cpp setJobManager ="<< jm << endl;
    jobManager = jm;
}

void Job::setJobManager(const char *jm) {
    jobManager = jm;
}

void Job::setJobManager(const char *jm, size_t jm_len) {
    jobManager.assign(jm, jm_len);
}

map<string, string>* Job::getEnv() {
	return env;
}

const string* Job::getEnv(string name) const {
	if (env == NULL) {
		return NULL;
	}
	else {
		map<string, string>::iterator it;
		it = env->find(name);
		if (it == env->end()) {
			return NULL;
		}
		else {
			return &(it->second);
		}
	}
}

void Job::setEnv(string name, string value) {
	if (env == NULL) {
		env = new map<string, string>;
	}
	env->insert(pair<string, string>(name, value));
}

void Job::setEnv(const char *name, size_t name_len,
		 const char *value, size_t value_len) {
	if (env == NULL) {
		env = new map<string, string>;
	}
	env->insert(pair<string, string>(string(name, name_len),
					 string(value, value_len)));
}

map<string, string>* Job::getAttributes() {
	return attributes;
}

const string* Job::getAttribute(string name) {
	if (attributes == NULL) {
		return NULL;
	}
	else {
		map<string, string>::iterator it;
		it = attributes->find(name);
		if (it == attributes->end()) {
			return NULL;
		}
		else {
			return &(it->second);
		}
	}
}

void Job::setAttribute(string name, string value) {
	if (attributes == NULL) {
		attributes = new map<string, string>;
	}
	attributes->insert(pair<string, string>(name, value));
}

void Job::setAttribute(const char *name, size_t name_len,
		       const char *value, size_t value_len) {
	if (attributes == NULL) {
		attributes = new map<string, string>;
	}
	attributes->insert(pair<string, string>(string(name, name_len),
					    string(value, value_len)));
}

vector<StagingSetEntry>* Job::getStageIns() {
	return stageIns;
}

void Job::addStageIn(string src, string dest, CoasterStagingMode mode) {
	if (stageIns == NULL) {
		stageIns = new vector<StagingSetEntry>;
	}
	stageIns->push_back(StagingSetEntry(src, dest, mode));
}

vector<StagingSetEntry>* Job::getStageOuts() {
	return stageOuts;
}

void Job::addStageOut(string src, string dest, CoasterStagingMode mode) {
	if (stageOuts == NULL) {
		stageOuts = new vector<StagingSetEntry>;
	}
	stageOuts->push_back(StagingSetEntry(src, dest, mode));
}

vector<string>* Job::getCleanups() {
	return cleanups;
}

void Job::addCleanup(string cleanup) {
	if (cleanups == NULL) {
		cleanups = new vector<string>;
	}
	cleanups->push_back(cleanup);
}

void Job::addCleanup(const char *cleanup, size_t cleanup_len) {
	if (cleanups == NULL) {
		cleanups = new vector<string>;
	}
	cleanups->push_back(string(cleanup, cleanup_len));
}

const JobStatus* Job::getStatus() const {
	return status;
}

const string* Job::getStdout() const {
	return stdout;
}
const string* Job::getStderr() const {
	return stderr;
}

/*
 * Just include the executable and arguments for now
 */
string Job::toString() const {
	stringstream ss;
	ss << executable;
	for (vector<string*>::const_iterator it = arguments.begin();
	     it != arguments.end(); ++it) {
		const string *arg = *it;
		if (arg == NULL) {
			ss << " NULL";	
		} else {
			ss << " " << *arg ;
		}
	}
	return ss.str();
}

void Job::setStatus(JobStatus* newStatus) {
	// Since the client can process a job status while another
	// status is coming in, a status cannot be deleted when a new status comes in.
	// Instead, all statuses get chained and all get de-allocated
	// when the job is de-allocated.
	newStatus->setPreviousStatus(status);
	status = newStatus;
}

Job::~Job() {
	if (status != NULL) {
		delete status;
	}
	if (stdinLocation != NULL) {
		delete stdinLocation;
	}
	if (stdoutLocation != NULL) {
		delete stdoutLocation;
	}
	if (stderrLocation != NULL) {
		delete stderrLocation;
	}
	for (unsigned int i = 0; i < arguments.size(); i++) {
		delete arguments.at(i);
	}
}
