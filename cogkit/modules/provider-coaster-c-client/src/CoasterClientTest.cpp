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
#include <list>

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

int main(void) {
	try {
		CoasterLoop loop;
		loop.start();

		CoasterClient client("localhost:53001", loop);
		client.start();

		Settings s;
		s.set(Settings::Key::SLOTS, "1");
		s.set(Settings::Key::MAX_NODES, "1");
		s.set(Settings::Key::JOBS_PER_NODE, "2");

		std::string configId = client.setOptions(s);

		Job j1("/bin/date");
		Job j2("/bin/echo");
		j2.addArgument("testing");
		j2.addArgument("1, 2, 3");

		client.submit(j1, configId);
		client.submit(j2, configId);

		client.waitForJob(j1);
		client.waitForJob(j2);
		list<Job*>* doneJobs = client.getAndPurgeDoneJobs();
		
		delete doneJobs;

		if (j1.getStatus()->getStatusCode() == JobStatus::FAILED) {
			cerr << "Job 1 failed: " << *j1.getStatus()->getMessage() << endl;
		}
		if (j2.getStatus()->getStatusCode() == JobStatus::FAILED) {
			cerr << "Job 2 failed: " << *j2.getStatus()->getMessage() << endl;
		}

		cout << "All done" << endl;

		client.stop();
		loop.stop();
		return EXIT_SUCCESS;
	}
	catch (exception& e) {
		cerr << "Exception caught: " << e.what() << endl;
		return EXIT_FAILURE;
	}
}
