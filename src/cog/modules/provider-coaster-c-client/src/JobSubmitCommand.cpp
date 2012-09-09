#include "JobSubmitCommand.h"
#include <cstring>
#include <sstream>

using namespace std;

char* copyStr(const char* str);
void add(stringstream& ss, const char* key, string* value);
void add(stringstream& ss, const char* key, string value);
void add(stringstream& ss, const char* key, const char* value);

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

void JobSubmitCommand::serialize() {
	addOutData(Buffer::wrap(getName()));

	stringstream ss;

	add(ss, "identity", job->getIdentity());
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
			stringstream tmp;
			tmp << i->first;
			tmp << "=";
			tmp << i->second;
			add(ss, "env", tmp.str().c_str());
		}
	}

	map<string, string>* attributes = job->getAttributes();
	if (attributes != NULL) {
		for (map<string, string>::iterator i = attributes->begin(); i != attributes->end(); ++i) {
			stringstream tmp;
			tmp << i->first;
			tmp << "=";
			tmp << i->second;
			add(ss, "attr", tmp.str().c_str());
		}
	}

	if (job->getJobManager() == NULL) {
		add(ss, "jm", "fork");
	}
	else {
		add(ss, "jm", job->getJobManager());
	}

	addOutData(Buffer::wrap(ss.str()));
}

void add(stringstream& ss, const char* key, string* value) {
	if (value != NULL) {
		add(ss, key, value->c_str());
	}
}

void add(stringstream& ss, const char* key, string value) {
	add(ss, key, value.c_str());
}

void add(stringstream& ss, const char* key, const char* value) {
	if (value != NULL) {
		ss << key << "=";
		while (*value) {
			char c = *value;
			switch (c) {
				case '\n':
					c = 'n';
				case '\\':
					ss << '\\';
				default:
					ss << c;
			}
			value++;
		}
	}
}
