#include "Command.h"

#include "Logger.h"

Command::Command(const string* pname) {
	name = pname;
}

const string* Command::getName() {
	return name;
}

void Command::send(CoasterChannel* channel) {
	send(channel, NULL);
}

ostream& operator<< (ostream& os, Command* cmd) {
	os << "Command[" << cmd->getName() << ", tag: " << cmd->getTag() << "]";
	return os;
}


void Command::send(CoasterChannel* channel, CommandCallback* pcb) {
	cb = pcb;
	channel->registerCommand(this);

	vector<Buffer*>* od = getOutData();
	vector<Buffer*>::iterator i;

	channel->send(tag, Buffer::wrap(name), od->empty() ? FLAG_FINAL : 0, this);

	for (i = od->begin(); i != od->end(); i++) {
		channel->send(tag, *i, i == --od->end() ? FLAG_FINAL : 0, this);
	}
}

void Command::receiveCompleted(int flags) {
	if (flags & FLAG_ERROR != 0) {
		errorReceived();
	}
	else {
		replyReceived();
	}
}


void Command::errorReceived() {
	vector<Buffer*>* errorData = getErrorData();
	if (cb != NULL) {
		if (errorData == NULL) {
			cb->errorReceived(this, NULL, NULL);
		}
		else if (errorData->size() == 1) {
			string* msg = errorData->at(0)->str();
			cb->errorReceived(this, msg, NULL);
			delete msg;
		}
		else {
			string* msg = errorData->at(0)->str();
			string* detail = errorData->at(1)->str();
			cb->errorReceived(this, msg, detail);
			delete msg;
			delete detail;
		}
	}
}

void Command::replyReceived() {
	if (cb != NULL) {
		cb->replyReceived(this);
	}
}

void Command::dataSent(Buffer* buf) {
	delete buf;
}
