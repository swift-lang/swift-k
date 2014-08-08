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



#include <getopt.h>
#include <stdlib.h>
#include <list>
#include <string.h>

#include "CoasterError.h"
#include "CoasterLoop.h"
#include "CoasterClient.h"
#include "Job.h"
#include "Settings.h"
#include "Logger.h"

using namespace Coaster;

using std::cerr;
using std::cout;
using std::endl;
using std::exception;
using std::list;
using std::string;
using std::vector;

static struct option long_options[] = {
   {"service", required_argument, 0, 's'},
   {"job-manager", required_argument, 0, 'j'},
   {"env", required_argument, 0, 'e'},
   {"attr",  required_argument, 0, 'a'},
   {"option",  required_argument, 0, 'o'},
   {"dir",  required_argument, 0, 'd'},
   {"stdout",  required_argument, 0, 'O'},
   {"stderr",  required_argument, 0, 'E'},
   {"verbosity",  required_argument, 0, 'v'},
   {"help",  no_argument, 0, 'h'},
   {0, 0, 0, 0}
};

struct KV {
	char* key;
	char* value;
};

void parsePair(char* s, KV* pair);
void parseArguments(int argc, char* argv[]);
void checkArguments();
void configureLogging();
int runJob();
void displayHelp();

char* serviceUrl = NULL;
list<char*> envs;
list<char*> attrs;
list<char*> options;
char* executable = NULL;
list<char*> args;
char* jobManager = NULL;
char* stdoutLoc = NULL;
char* stderrLoc = NULL;
char* verbosity = NULL;
string* dir = NULL;

int main(int argc, char* argv[]) {
	try {
		parseArguments(argc, argv);
		checkArguments();
		configureLogging();
		return runJob();
	}
	catch (exception& e) {
		cerr << "Exception caught: " << e.what() << endl;
		return EXIT_FAILURE;
	}
}

void parseArguments(int argc, char* argv[]) {
	int oindex, c;
	while (true) {
		c = getopt_long(argc, argv, "hs:e:a:o:j:d:O:E:v:", long_options, &oindex);
		if (c == -1) {
			break;
		}
		switch (c) {
			case 's':
				serviceUrl = optarg;
				break;
			case 'e':
				envs.push_back(optarg);
				break;
			case 'a':
				attrs.push_back(optarg);
				break;
			case 'o':
				options.push_back(optarg);
				break;
			case 'j':
				jobManager = optarg;
				break;
			case 'd':
				dir = new string(optarg);
				break;
			case 'O':
				stdoutLoc = optarg;
				break;
			case 'E':
				stderrLoc = optarg;
				break;
			case 'v':
				verbosity = optarg;
				break;
			case 'h':
				displayHelp();
				exit(0);
				break;
			case '?':
				if (optopt == 'c' || optopt == 'e' || optopt == 'a' || optopt == 'o'
						|| optopt == 'j' || optopt == 'O' || optopt == 'E') {
					cerr << "Missing argument for option '-" << (char) optopt << "'" << endl;
					exit(1);
				}
				else {
					exit(1);
				}
			default:
				cerr << "Unknown error parsing command line" << endl;
				exit(1);
		}
	}

	int index = optind;
	if (index == argc) {
		cerr << "Missing executable" << endl;
		exit(1);
	}
	executable = argv[index++];
	while (index < argc) {
		args.push_back(argv[index++]);
	}
}

void displayHelp() {
	cout << "Usage: run-coaster-job [OPTIONS] EXECUTABLE [ARGUMENTS]" << endl;
	cout << "where:" << endl << endl;
	cout << "\tEXECUTABLE   The executable to run (e.g. '/bin/date')" << endl;
	cout << "\tARGUMENTS    A list of arguments to pass to the EXECUTABLE" << endl;
	cout << "\tOPTIONS      An option. Short options use a space as separator" << endl;
	cout << "\t             between option name and value, while long options" << endl;
	cout <<	"\t             use the equal sign (e.g. --service=localhost). " << endl;
	cout << "\t             Can be one of the following:" << endl << endl;
	cout << "\t--help|-h     Displays this message" << endl << endl;
	cout << "\t--service|-s <host>[:<port>]" << endl;
	cout <<	"\t              REQUIRED. The location of the coaster service." << endl << endl;
	cout << "\t--job-manager|-j <value>" << endl;
	cout << "\t              The job manager to use." << endl << endl;
	cout << "\t--env|-e <name>=<value>" << endl;
	cout << "\t              An environment variable to be passed to the " << endl;
	cout << "\t              executable. Can be used more than once." << endl << endl;
	cout << "\t--attr|-a <name>=<value>" << endl;
	cout << "\t              A job attribute (such as 'maxwalltime')." << endl;
	cout << "\t              Can be used more than once." << endl << endl;
	cout << "\t--option|-o <name>=<value>" << endl;
	cout << "\t              An option to pass to the coaster service " << endl;
	cout << "\t              (such as 'slots=10'). Can be used more than" << endl;
	cout << "\t              once." << endl << endl;
	cout << "\t--stdout|-O <file>" << endl;
	cout << "\t              If present, the job standard output will be" << endl;
	cout << "\t              redirected to the specified remote file." << endl << endl;
	cout << "\t--stderr|-E <file>" << endl;
	cout << "\t              If present, the job standard error will be" << endl;
	cout << "\t              redirected to the specified remote file." << endl << endl;
	cout << "\t--verbosity|-v ['d'|'i'] " << endl;
	cout << "\t              Controls the verbosity of the logging messages" << endl;
	cout << "\t              printed on stdout. By default only WARN and ERROR" << endl;
	cout << "\t              levels are printed. 'i' further enables INFO" << endl;
	cout << "\t              message, while 'd' enables all messages." << endl << endl;
	cout << endl;
}

void checkArguments() {
	if (serviceUrl == NULL) {
		cerr << "Missing service argument" << endl;
		exit(1);
	}
	if (jobManager == NULL) {
		jobManager = (char*) malloc(16);
		strcpy(jobManager, "local");
	}
	if (verbosity == NULL) {
		verbosity = (char*) malloc(2);
		strcpy(verbosity, "w");
	}
	else {
		if (strcmp(verbosity, "i") && strcmp(verbosity, "d")) {
			cerr << "Invalid verbosity value (" << verbosity << "). Valid values are 'd' and 'i'" << endl;
			exit(1);
		}
	}
}

void configureLogging() {
	switch (*verbosity) {
		case 'w':
			Logger::singleton().setThreshold(Logger::WARN);
			break;
		case 'i':
			Logger::singleton().setThreshold(Logger::INFO);
			break;
		case 'd':
			Logger::singleton().setThreshold(Logger::DEBUG);
			break;
	}
}

int runJob() {
	CoasterLoop loop;
	loop.start();

	CoasterClient client(serviceUrl, loop);
	client.start();

	Settings s;
	list<char*>::iterator i;
	KV pair;
	for (i = options.begin(); i != options.end(); i++) {
		parsePair(*i, &pair);
		string* skey = new string(pair.key);
		s.set(*skey, pair.value);
	}

	std::string configId = client.setOptions(s);

	Job j(executable);

	if (dir != NULL) {
		j.setDirectory(*dir);
	}

	for (i = args.begin(); i != args.end(); i++) {
		j.addArgument(*i);
	}

	for (i = attrs.begin(); i != attrs.end(); i++) {
		parsePair(*i, &pair);
		j.setAttribute(pair.key, pair.value);
	}

	for (i = envs.begin(); i != envs.end(); i++) {
		parsePair(*i, &pair);
		j.setEnv(pair.key, pair.value);
	}

	if (stdoutLoc != NULL) {
		string* str = new string(stdoutLoc);
		j.setStdoutLocation(*str);
	}

	if (stderrLoc != NULL) {
		string* str = new string(stderrLoc);
		j.setStderrLocation(*str);
	}

        if (jobManager != NULL) {
		j.setJobManager(jobManager);
        }

	client.submit(j, configId);

	client.waitForJob(j);

	if (j.getStatus()->getStatusCode() == JobStatus::FAILED) {
		cerr << "Job failed: " << *j.getStatus()->getMessage() << endl;
		return 2;
	}

	cout << "Job completed" << endl;
	return EXIT_SUCCESS;
}

void parsePair(char* s, KV* pair) {
	pair->key = s;
	while (*s) {
		if (*s == '=') {
			*s = 0;
			pair->value = s + 1;
			return;
		}
		s++;
	}
	cerr << "Invalid argument value: " << s << endl;
	exit(1);
}
