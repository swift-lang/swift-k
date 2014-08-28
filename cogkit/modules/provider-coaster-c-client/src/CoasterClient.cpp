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


/*
 * coaster-client.c
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include "CoasterClient.h"
#include "JobSubmitCommand.h"
#include "ServiceConfigurationCommand.h"
#include "ChannelConfigurationCommand.h"
#include "CoasterError.h"
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include "ClientHandlerFactory.h"

#include "Logger.h"

using namespace Coaster;

using std::list;
using std::map;
using std::string;

CoasterClient::CoasterClient(string URL, CoasterLoop& ploop) {
	this->URL = URL;
	hostName = NULL;
	sockFD = 0;
	loop = &ploop;
	started = false;
	handlerFactory = new ClientHandlerFactory;
}

CoasterClient::~CoasterClient() {
	stop();
	if (hostName != NULL) {
		delete hostName;
	}
	delete handlerFactory;
}

void CoasterClient::start() {
	if (started) {
		return;
	}

	LogInfo << "Starting client " << getHostName() << endl;

	struct addrinfo* addrInfo;
	struct addrinfo* it;

	char* error = NULL;

	addrInfo = resolve(getHostName().c_str(), getPort());

	for (it = addrInfo; it != NULL; it = it->ai_next) {
		sockFD = socket(it->ai_family, it->ai_socktype, it->ai_protocol);

		if (sockFD == -1) {
			continue;
		}

		LogDebug << "Trying " << inet_ntoa(((sockaddr_in*) (it->ai_addr))->sin_addr) << endl;

		if (connect(sockFD, it->ai_addr, it->ai_addrlen) == 0) {
			LogDebug << "Connected" << endl;
			break;
		}

		error = strerror(errno);

		close(sockFD);
		sockFD = -1;
	}

	freeaddrinfo(addrInfo);

	if (sockFD == -1) {
		// none succeeded
		if (error != NULL) {
			throw CoasterError("Failed to connect to %s: %s", URL.c_str(), error);
		}
		else {
			throw CoasterError("Failed to connect to %s", URL.c_str());
		}
	}

	// configure non-blocking
	int flags = fcntl(sockFD, F_GETFL);
	if ((flags & O_NONBLOCK) == 0) {
		if (fcntl(sockFD, F_SETFL, flags | O_NONBLOCK) < 0) {
			throw CoasterError("Failed to configure socket for non-blocking operations: %s", strerror(errno));
		}
	}

	channel = new CoasterChannel(this, loop, handlerFactory);
	channel->setSockFD(sockFD);
	channel->start();
	loop->addChannel(channel);

	ChannelConfigurationCommand ccc;
	ccc.execute(channel);

	LogInfo << "Done" << endl;

	started = true;
}

void CoasterClient::stop() {
	if (!started) {
		return;
	}

	LogInfo << "Stopping client " << getHostName() << endl;

	channel->shutdown();
	loop->removeChannel(channel, true);

	LogInfo << "Done" << endl;

	started = false;
}

string CoasterClient::setOptions(Settings& s) {
	LogInfo << "Setting options: " << s << endl;
	ServiceConfigurationCommand scc(s);
	scc.execute(channel);
	string *id = scc.getConfigId();
	if (id == NULL) {
		throw CoasterError("Did not get expected response for config");
	}
	return *id;
}

void CoasterClient::submit(Job& job, const std::string& configId) {
	{ Lock::Scoped l(lock);
		jobs[job.getIdentity()] = &job;
	}
	JobSubmitCommand* sjc = new JobSubmitCommand(&job, configId);
	sjc->send(channel, this);
}

void CoasterClient::errorReceived(Command* cmd, string* message, RemoteCoasterException* details) {
	if (*(cmd->getName()) == JobSubmitCommand::NAME) {
		JobSubmitCommand* jsc = static_cast<JobSubmitCommand*>(cmd);
		coaster_job_id jobId = jsc->getJob()->getIdentity();
		LogInfo << "Job " << jobId << " failed: " << message << "\n" << details->str() << endl;
		updateJobStatus(jobId, new JobStatus(JobStatus::FAILED, message, details));
	}
	else {
		LogWarn << "Error received for command " << cmd;
		if (message != NULL) {
			LogWarn << ": " << message;
		}
		if (details != NULL) {
			LogWarn << "\n" << message;
		}
		LogWarn << endl;
	}

	delete cmd;
}

void CoasterClient::replyReceived(Command* cmd) {
	if (*(cmd->getName()) == JobSubmitCommand::NAME) {
		JobSubmitCommand* jsc = static_cast<JobSubmitCommand*>(cmd);
		string remoteId = jsc->getRemoteId();
		Job *job = jsc->getJob();
		coaster_job_id jobId = job->getIdentity();
		LogInfo << "Job " << jobId << " submitted; remoteId: " << remoteId << endl;

		// Track relationship between both IDs
		job->setRemoteIdentity(remoteId);
		remoteJobIdMapping[remoteId] = jobId;
		updateJobStatus(jobId, new JobStatus(JobStatus::SUBMITTED));
	}
	delete cmd;
}

/*
  Internal function, should be called with lock already held.
 */
void CoasterClient::updateJobStatusNoLock(Job* job, JobStatus* status) {
	job->setStatus(status);
	if (status->isTerminal()) {
		// Remote id should have been set in job
		const string *remoteId = job->getRemoteIdentity();
		if (remoteId) {
			remoteJobIdMapping.erase(*remoteId);
		}
		jobs.erase(job->getIdentity());
		doneJobs.push_back(job);
		cv.broadcast();
	}
}

void CoasterClient::updateJobStatus(const string& remoteJobId, JobStatus* status) { Lock::Scoped l(lock);
	if (remoteJobIdMapping.count(remoteJobId) == 0) {
		LogWarn << "Received job status notification for unknown job (" << remoteJobId << "): " << status << endl;
	}
	else {
		coaster_job_id jobId = remoteJobIdMapping[remoteJobId];
		updateJobStatusNoLock(jobs[jobId], status);
	}
}

void CoasterClient::updateJobStatus(coaster_job_id jobId, JobStatus* status) { Lock::Scoped l(lock);
	updateJobStatusNoLock(jobs[jobId], status);
}

void CoasterClient::updateJobStatus(Job* job, JobStatus* status) { Lock::Scoped l(lock);
	updateJobStatusNoLock(job, status);
}

int CoasterClient::getPort() {
	size_t index;

	index = URL.find(':');
	if (index == string::npos) {
		// default port
		return 53001;
	}
	else {
		// need to have string as local variable to prevent free
		string sport = URL.substr(index + 1);
		return atoi(sport.c_str());
	}
}

const string& CoasterClient::getHostName() {
	size_t index;

	if (hostName == NULL) {
		index = URL.find(':');
		if (index == string::npos) {
			hostName = new string(URL);
		}
		else {
			hostName = new string(URL.substr(0, index));
		}
	}
	return *hostName;
}

struct addrinfo* CoasterClient::resolve(const char* hostName, int port) {
	struct addrinfo hints;
	struct addrinfo* info;

	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = 0;
	hints.ai_protocol = 0;

	char* sPort = (char *) malloc(sizeof(int) * 8 + 1);
	sprintf(sPort, "%d", port);

	int result = getaddrinfo(hostName, sPort, &hints, &info);

	if (result != 0) {
		throw CoasterError("Host name lookup failure: %s", gai_strerror(result));
	}

	free(sPort);

	return info;
}

/**
 * Retrieve the list of completed/failed/canceled jobs. If no more jobs
 * finish after this call, a subsequent call to this method will return
 * NULL. It's the caller's responsibility to delete the returned list.
 */
list<Job*>* CoasterClient::getAndPurgeDoneJobs() { Lock::Scoped l(lock);
	if (doneJobs.size() == 0) {
		return NULL;
	}
	else {
		list<Job*>* l = new list<Job*>(doneJobs);
		doneJobs.clear();
		return l;
	}
}

int CoasterClient::getAndPurgeDoneJobs(int size, Job** jobs)
				      { Lock::Scoped l(lock);
	int count = 0;
	while (count < size && !doneJobs.empty()) {
		jobs[count++] = doneJobs.front();
		doneJobs.pop_front();
	}
	return count;
}

void CoasterClient::waitForJobs() { Lock::Scoped l(lock);
	while (jobs.size() != 0) {
		cv.wait(lock);
	}
}

void CoasterClient::waitForAnyJob() { Lock::Scoped l(lock);
	while (doneJobs.size() == 0) {
		cv.wait(lock);
	}
}

void CoasterClient::waitForJob(const Job& job) { Lock::Scoped l(lock);
	while (jobs.count(job.getIdentity()) != 0) {
		cv.wait(lock);
	}
}

const string& CoasterClient::getURL() {
	return URL;
}
