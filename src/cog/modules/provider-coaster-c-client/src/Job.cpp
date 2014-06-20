#include "Job.h"
#include <sstream>
#include <cstring>

using namespace std;

static int seq = 0;

Job::Job(const string &pexecutable) {
	executable = pexecutable;
	stringstream ss;
	ss << "job-";
	ss << seq++;
	identity = ss.str();

	arguments = NULL;
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

const string& Job::getIdentity() const {
	return identity;
}

vector<string*>* Job::getArguments() {
	return arguments;
}

void Job::addArgument(string& arg) {
	if (arguments == NULL) {
		arguments = new vector<string*>;
	}
	arguments->push_back(new string(arg));
}

const string& Job::getExecutable() const {
	return executable;
}

void Job::addArgument(const char* arg) {
	addArgument(*(new string(arg)));
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

vector<StagingSetEntry>* Job::getStageIns() {
	return stageIns;
}

void Job::addStageIn(string src, string dest, StagingMode mode) {
	if (stageIns == NULL) {
		stageIns = new vector<StagingSetEntry>;
	}
	stageIns->push_back(StagingSetEntry(src, dest, mode));
}

vector<StagingSetEntry>* Job::getStageOuts() {
	return stageOuts;
}

void Job::addStageOut(string src, string dest, StagingMode mode) {
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

const JobStatus* Job::getStatus() const {
	return status;
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
	if (arguments != NULL) {
		for (int i = 0; i < arguments->size(); i++) {
			delete arguments->at(i);
		}
		delete arguments;
	}
}
