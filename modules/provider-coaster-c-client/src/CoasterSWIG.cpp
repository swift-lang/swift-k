#include <iostream>
#include <stdlib.h>
#include <string>
#include <string.h>
#include <sstream>
#include <vector>

#include <CoasterSWIG.h>
#include "CoasterLoop.h"
#include "CoasterClient.h"
#include "Settings.h"
#include "Job.h"

using namespace Coaster;

/** CoasterSWIGLoopCreate : create, starts and returns
 *  a pointer to a CoasterLoop object.
 */
CoasterLoop* CoasterSWIGLoopCreate(void)
{
    cout << "CoasterSWIGLoopCreate()..." << endl;
    CoasterLoop *loop = new CoasterLoop();
    loop->start();
    return loop;
}

/* Destroys the loop
 */
int CoasterSWIGLoopDestroy(CoasterLoop* loop)
{
    delete(loop);
    return 0;
}

CoasterClient* CoasterSWIGClientCreate(CoasterLoop *loop, char* serviceURL)
{
    cout << "CoasterSWIGClientCreate(" << serviceURL << ")..." << endl;
    CoasterClient* client = new CoasterClient(serviceURL, *loop);
    client->start();
    return client;
}

int CoasterSWIGClientDestroy(CoasterClient *client)
{
    delete(client);
    return 0;
}

int CoasterSWIGClientSettings(CoasterClient *client, char *settings)
{
    Settings s;
    int kv_index = 0;
    cout << "CoasterSWIGClientSettings(" << settings << ")" <<endl;
    cout << "Client : [" << (void *) client << "]"<<endl;

    // Parsing logic
    // K1=V1, K2=V2 is the format of the settings string
    std::vector<std::string> elems;
    std::stringstream ss(settings);
    std::string item;
    // Assumed to not have more than 50 coaster settings
    std::string kv_pair[50][2];

    while (std::getline(ss, item, ',')) {
        elems.push_back(item);
        std::stringstream kv(item);
        std::string kv_item;
        std::getline(kv, kv_pair[kv_index][0], '=');
        std::getline(kv, kv_pair[kv_index][1]);
        s.set(kv_pair[kv_index][0], kv_pair[kv_index][1]);
        cout << "Key,Value : " << kv_pair[kv_index][0] <<", "<< kv_pair[kv_index][1] << endl;
        kv_index++;
    }

    client->setOptions(s);
    /*  ONLY FOR DEBUGGING
    map<string*, const char*>::iterator i;
    map<string *, const char*>* m = s.getSettings();
    for (i = m->begin(); i != m->end(); i++) {
        string ss;
        cout << "Key, Value SET : " << *(i->first) << " : " << i->second << endl;
    }
    */
    return 0;
}

Job* CoasterSWIGJobCreate(char *cmd_string, char *jobmanager)
{
    string jm = string(jobmanager);
    cout << "CoasterSWIGJobCreate("<< cmd_string <<") : jobmanager="<< jm << endl;
    Job *job = new Job(cmd_string);
    job->setJobManager(jm);
    return job;
}

struct KV {
    std::string key;
    std::string value;
};

std::vector<struct KV> parse_kvpair (std::string str)
{
    std::vector<struct KV> pairs;
    std::stringstream ss(str);
    std::string item, key, value;

    // TODO: should this handle quotes or escape sequences?
    while (std::getline(ss, item, ',')) {
        //elems.push_back(item);
        struct KV kv_pair;
        std::stringstream kv(item);
        std::string kv_item;
        std::getline(kv, kv_item, '=');
        kv_pair.key = kv_item;
        std::getline(kv, kv_item);
        kv_pair.value =  kv_item;
        pairs.push_back(kv_pair);
        std::cout << "Key,Value : " << key <<", " << value << std::endl;
    }
    return pairs;
}

std::vector<string> parse_string (std::string str)
{
    std::vector<string> strings;
    std::stringstream ss(str);
    std::string item, key, value;

    while (std::getline(ss, item, ',')) {
        strings.push_back(item);
    }
    return strings;
}

int CoasterSWIGJobSettings(Job* j, char* dir, char* args, char* attr,
                           char* envs, char *stdoutLoc, char *stderrLoc)
{
    std::vector<struct KV> pairs;

    if ( strlen(dir) != 0 ){
        string str = string(dir);
        j->setDirectory(str);
    }

    if ( strlen(args) != 0 ){
        /*
        std::vector<string> arg_vector = parse_string(string(args));
        for (std::vector<string>::iterator i = arg_vector.begin(); i != arg_vector.end(); i++ ){
            j->addArgument(*i);
            cout << "Attr ["<< *i <<"]" <<endl;
        }
        */
        j->addArgument(args);
    }

    // Untested code block ahead.
    /*
    if ( strlen(attr) != 0 ){
        pairs = parse_kvpair (string(attr));
        for (std::vector<struct KV>::iterator i = pairs.begin(); i != pairs.end(); i++ ){
            j->setAttribute(i.key, i.value);
            cout << "Attr ["<< i.key <<":" << i.value <<"]" <<endl;
        }
    }

    if ( strlen(envs) != 0 ){
        pairs = parse_kvpair (string(envs));
        for (std::vector<struct KV>::iterator i = pairs.begin(); i != pairs.end(); i++ ){
            j->setEnv(i.key, i.value);
            cout << "Env ["<< i.key <<":" << i.value <<"]" <<endl;
        }
    }
    */
    if (strlen(stdoutLoc) != 0 ) {
        string* str = new string(stdoutLoc);
        j->setStdoutLocation(*str);
    }

    if (strlen(stderrLoc) != 0 ) {
        string* str = new string(stderrLoc);
        j->setStderrLocation(*str);
    }

    return 0;
}

int CoasterSWIGSubmitJob(CoasterClient *client, Job* job)
{
    client->submit(*job);
    return 0;
}

int CoasterSWIGWaitForJob(CoasterClient *client, Job *job)
{
    client->waitForJob(*job);
    int status = job->getStatus()->getStatusCode();
    cout << "SubmitJob returns code :" << status << endl;
    return status;
}

int CoasterSWIGGetJobStatus(CoasterClient *client, Job *job)
{
    // TODO  : Check job->getStatus() for NULL before trying for getStatusCode
    int status = 9999;
    if ( job->getStatus() != NULL ){
        status = job->getStatus()->getStatusCode();
    }
    cout << "SubmitJob returns code :" << status << endl;
    //    cerr << "Job status message     :" << job->getStatus()->getMessage() << endl;
    return status;
}

/**
 * This is a test function.
 */
int CoasterSWIGTest (CoasterLoop *loop, char *serviceURL, CoasterClient *client)
{
	try {
        //CoasterClient* client = new CoasterClient(serviceURL, *loop);

        //CoasterClient* ptr = &client;
        CoasterClient* ptr = client;
        //client.start();
        //CoasterClient* client = new CoasterClient(serviceURL, *loop);

		Settings s;
		s.set(Settings::Key::SLOTS, "1");
		s.set(Settings::Key::MAX_NODES, "1");
		s.set(Settings::Key::JOBS_PER_NODE, "2");

		//client->setOptions(s);
        //client.setOptions(s);
        ptr->setOptions(s);

		Job j1("/bin/date");
		Job j2("/bin/echo");
		j2.addArgument("testing");
		j2.addArgument("1, 2, 3");


		ptr->submit(j1);
		ptr->submit(j2);

		ptr->waitForJob(j1);
		ptr->waitForJob(j2);
		list<Job*>* doneJobs = ptr->getAndPurgeDoneJobs();
        /*
        client.submit(j1);
		client.submit(j2);

		client.waitForJob(j1);
		client.waitForJob(j2);
		list<Job*>* doneJobs = client.getAndPurgeDoneJobs();
        */
		delete doneJobs;

		if (j1.getStatus()->getStatusCode() == FAILED) {
			cerr << "Job 1 failed: " << *j1.getStatus()->getMessage() << endl;
		}
		if (j2.getStatus()->getStatusCode() == FAILED) {
			cerr << "Job 2 failed: " << *j2.getStatus()->getMessage() << endl;
		}

		cout << "All done" << endl;

		return EXIT_SUCCESS;
	}
	catch (exception& e) {
		cerr << "Exception caught: " << e.what() << endl;
		return EXIT_FAILURE;
	}
}


