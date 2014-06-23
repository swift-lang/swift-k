#include "JobSubmitCommand.h"
#include "CoasterError.h"
#include <cstring>
#include <string>

using namespace std;

void add(string& ss, const char* key, const string* value);
void add(string& ss, const char* key, const string& value);
void add(string& ss, const char* key, const char* value);
void add(string& ss, const char* key, const char* value, int n);

string JobSubmitCommand::NAME("SUBMITJOB");

JobSubmitCommand::JobSubmitCommand(Job* pjob): Command(&NAME) {
	job = pjob;
}

void JobSubmitCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	serialize();
	Command::send(channel, cb);
}

Job* JobSubmitCommand::getJob() {
	return job;
}

string* JobSubmitCommand::getRemoteId() {
	if (!isReceiveCompleted() || isErrorReceived()) {
		throw CoasterError("getRemoteId called before reply was received");
	}
	return getInData()->at(0)->str();
}

void JobSubmitCommand::serialize() {
        stringstream idSS;
        idSS << job->getIdentity();
	add(ss, "identity", idSS.str());
	add(ss, "executable", job->getExecutable());
	add(ss, "directory", job->getDirectory());

	add(ss, "stdin", job->getStdinLocation());
	add(ss, "stdout", job->getStdoutLocation());
	add(ss, "stderr", job->getStderrLocation());


	vector<string*>* arguments = job->getArguments();
	if (arguments != NULL) {
		for (vector<string*>::iterator i = arguments->begin(); i != arguments->end(); ++i) {
			add(ss, "arg", *i);
		}
	}

	map<string, string>* env = job->getEnv();
	if (env != NULL) {
		for (map<string, string>::iterator i = env->begin(); i != env->end(); ++i) {
			add(ss, "env", string(i->first).append("=").append(i->second));
		}
	}

	map<string, string>* attributes = job->getAttributes();
	if (attributes != NULL) {
		for (map<string, string>::iterator i = attributes->begin(); i != attributes->end(); ++i) {
			add(ss, "attr", string(i->first).append("=").append(i->second));
		}
	}

	if (job->getJobManager().empty()) {
        cout<< "getJobManager == NULL, setting to :  fork "<< endl;
		add(ss, "jm", "fork");
	}
	else {
        const char *jm_string = (job->getJobManager()).c_str();
        cout<< "getJobManager != !NULL, setting to : "<< job->getJobManager() << endl;
		add(ss, "jm", jm_string);
	}
	addOutData(Buffer::wrap(ss));
}

void add(string& ss, const char* key, const string* value) {
	if (value != NULL) {
		add(ss, key, value->data(), value->length());
	}
}

void add(string& ss, const char* key, const string& value) {
	add(ss, key, value.data(), value.length());
}

void add(string& ss, const char* key, const char* value) {
	add(ss, key, value, -1);
}

void add(string& ss, const char* key, const char* value, int n) {
	if (value != NULL && n != 0) {
		ss.append(key);
		ss.append(1, '=');
		while (*value) {
			char c = *value;
                        // TODO: are fallthroughs deliberate?
			switch (c) {
				case '\n':
					c = 'n';
				case '\\':
					ss.append(1, '\\');
				default:
					ss.append(1, c);
			}
			value++;
			n--;
		}
	}
	ss.append(1, '\n');
}
