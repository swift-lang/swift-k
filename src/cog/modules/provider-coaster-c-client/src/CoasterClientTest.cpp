
#include <stdio.h>
#include <stdlib.h>
#include <list>

#include "CoasterLoop.h"
#include "CoasterClient.h"
#include "Job.h"

using namespace std;

int main(void) {
	try {
		CoasterLoop loop = CoasterLoop();
		loop.start();

		CoasterClient client("localhost:1984", loop);
		client.start();

		Job j1("/bin/date");
		Job j2("/bin/echo");
		j2.addArgument("testing");
		j2.addArgument("1, 2, 3");

		client.submit(j1);
		client.submit(j2);

		client.waitForJob(j1);
		client.waitForJob(j2);
		list<Job*>* doneJobs = client.getAndPurgeDoneJobs();

		printf("All done\n");

		client.stop();
		loop.stop();

		return EXIT_SUCCESS;
	}
	catch (exception& e) {
		cerr << "Exception caught: " << e.what() << endl;
		return EXIT_FAILURE;
	}
}
