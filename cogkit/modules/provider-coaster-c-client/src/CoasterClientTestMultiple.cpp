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



#include <stdlib.h>
#include <unistd.h>
#include <list>
#include <stdexcept>
#include <string>
#include <sstream>

#include "CoasterLoop.h"
#include "CoasterClient.h"
#include "Job.h"
#include "Settings.h"

using namespace Coaster;

using std::cerr;
using std::cout;
using std::endl;
using std::exception;
using std::list;

void runJobs(CoasterClient& client, std::string& configId, int count, int concurrency, int delay);

void submitJob(CoasterClient& client, std::string& configId, int delay);

int purgeJobs(CoasterClient& client, bool nullOk);

int main(int argc, char** argv) {
	try {
		if (argc != 8) {
			throw std::runtime_error("Invalid number of arguments. Expected <URL> <totalJobs> <concurrency> <sleepDelay> <slots> <maxNodes> <jobsPerNode>");
		}
		
		int totalJobs, concurrency, sleepDelay;
		
		totalJobs = atoi(argv[2]);
		concurrency = atoi(argv[3]);
		sleepDelay = atoi(argv[4]);
		
		CoasterLoop loop;
		loop.start();

		CoasterClient client(argv[1], loop);
		client.start();
		
		cout << "Client started" << endl;

		Settings s;
		s.set(Settings::Key::SLOTS, argv[5]);
		s.set(Settings::Key::MAX_NODES, argv[6]);
		s.set(Settings::Key::JOBS_PER_NODE, argv[7]);
		s.set(Settings::Key::PROVIDER, "local");

		std::string configId = client.setOptions(s);
		
		cout << "Options set" << endl;
		
		runJobs(client, configId, totalJobs, concurrency, sleepDelay);

		cout << "All done" << endl;
		
		usleep(1000 * 1000);

		client.stop();
		cout << "Client stopped" << endl;
		loop.stop();
		cout << "Loop stopped" << endl;
		return EXIT_SUCCESS;
	}
	catch (exception& e) {
		cerr << "Exception caught: " << e.what() << endl;
		return EXIT_FAILURE;
	}
}

void runJobs(CoasterClient& client, std::string& configId, int count, int concurrency, int delay) {
	int running = 0;
	cout << "Concurrency: " << concurrency << ", delay: " << delay << endl;
	while (count > 0) {
		while (running < concurrency) {
			submitJob(client, configId, delay);
			running++;
		}
		//cout << "\r                                                                                                                 ";
		//cout << "\rLeft: " << count << ", running: " << running << "  " << std::flush;
		cout << "Left: " << count << ", running: " << running << "  " << endl;
		
		client.waitForAnyJob();
		
		int done = purgeJobs(client, false);
		count -= done;
		running -= done;
	}
	client.waitForJobs();
	purgeJobs(client, true);
}

int purgeJobs(CoasterClient& client, bool nullOk) {
	std::list<Job*>::const_iterator i;
	std::list<Job*>* done = client.getAndPurgeDoneJobs();
	if (done == NULL) {
		if (nullOk) {
			return 0;
		}
		else {
			throw std::runtime_error("Got null list from getAndPurgeDoneJobs()");
		}
	}
	int count = done->size();
	for (i = done->begin(); i != done->end(); i++) {
		Job* job = *i;
			
		if (job->getStatus()->getStatusCode() == JobStatus::FAILED) {
			cerr << "Job failed: " << *job->getStatus()->getMessage() << endl;
		}
			
		delete job;
	}
	delete done;
	return count;
}


void submitJob(CoasterClient& client, std::string& configId, int delay) {
	Job* j = new Job("/bin/sleep");
	std::stringstream ss;
	ss << delay;
	j->addArgument(ss.str());
	client.submit(*j, configId);
}