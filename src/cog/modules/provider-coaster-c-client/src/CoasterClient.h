/*
 * coaster-client.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef COASTER_CLIENT_H_
#define COASTER_CLIENT_H_

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

		map<const string*, Job*> jobs;
		map<string, const string*> remoteJobIdMapping;

		list<Job*> doneJobs;
	public:
		CoasterClient(string URL, CoasterLoop& loop);
		virtual ~CoasterClient();
		void start();
		void stop();

		void setOptions(Settings& settings);
		void submit(Job& job);
		void waitForJob(Job& job);

		list<Job*>* getAndPurgeDoneJobs();
		void waitForJobs();

		void updateJobStatus(string& jobId, JobStatus* status);

		string& getURL();

		void errorReceived(Command* cmd, string* message, RemoteCoasterException* details);
		void replyReceived(Command* cmd);
};

#endif /* COASTER_CLIENT_H_ */
