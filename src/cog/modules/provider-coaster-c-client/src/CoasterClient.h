/*
 * coaster-client.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef COASTER_CLIENT_H_
#define COASTER_CLIENT_H_

#ifndef SWIG

#include <string>
#include "Lock.h"
#include "ConditionVariable.h"
#include "Command.h"
#include "CoasterChannel.h"
#include "CoasterLoop.h"
#include "HandlerFactory.h"
#include "Job.h"
#include "Settings.h"
#include <list>
#include <map>

#include <netdb.h>

#endif 

using namespace std;

class ClientHandlerFactory;
class HandlerFactory;
class CoasterLoop;
class CoasterChannel;

class CoasterClient: public CommandCallback {
	private:
		Lock lock;
		ConditionVariable cv;
		string URL;
		string* hostName;
		CoasterChannel* channel;
		bool started;

		int sockFD;

		int getPort();
		string& getHostName();
		struct addrinfo* resolve(const char* hostName, int port);

		CoasterLoop* loop;
		HandlerFactory* handlerFactory;

                // TODO: won't pointer here use pointer comparison?
		map<const string*, Job*> jobs;
		map<string, const string*> remoteJobIdMapping;

		list<Job*> doneJobs;
	public:
		CoasterClient(string URL, CoasterLoop& loop);
		virtual ~CoasterClient();
		void start();
		void stop();

		void setOptions(Settings& settings);

                /*
                 * Submit a job.  The job should have been filled in with
                 * all properties.  The ownership of the job object stays
                 * with the caller, but this client will retain a reference
                 * to the job until done jobs are purged.
                 */
		void submit(Job& job);

                /*
                 * Wait for job to be done.  Upon completion no actions
                 * are taken: job must be purged from client explicitly.
                 */
		void waitForJob(const Job& job);

                /*
                 * Return a list of done jobs and remove references from this
                 * client.
                 */
		list<Job*>* getAndPurgeDoneJobs();
		void waitForJobs();

		void updateJobStatus(const string &jobId, JobStatus* status);

		const string& getURL();

		void errorReceived(Command* cmd, string* message, RemoteCoasterException* details);
		void replyReceived(Command* cmd);
};

#endif /* COASTER_CLIENT_H_ */
