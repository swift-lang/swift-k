#include "Handler.h"

#include "Logger.h"

void Handler::receiveCompleted(int flags) {
	if (flags & FLAG_ERROR) {
		errorReceived();
	}
	else {
		requestReceived();
	}
}

void Handler::errorReceived() {
	vector<Buffer*>* errorData = getErrorData();
	if (errorData == NULL) {
		LogWarn << "Unspecified error receiving request." << endl;
	}
	else if (errorData->size() == 1) {
		string* msg = errorData->at(0)->str();
		LogWarn << "Error receiving request: " << msg << endl;
		delete msg;
	}
	else {
		string* msg = errorData->at(0)->str();
		string* detail = errorData->at(1)->str();
		LogWarn << "Error receiving request: " << msg << "\n" << detail << endl;
		delete msg;
		delete detail;
	}
}

void Handler::requestReceived() {
	sendReply("OK");
}

void Handler::sendReply(string& msg) {
	addOutData(Buffer::wrap(msg));
	send(getChannel());
}

void Handler::sendReply(const char* msg) {
	addOutData(Buffer::wrap(msg));
	send(getChannel());
}

void Handler::send(CoasterChannel* channel) {
	list<Buffer*>* od = getOutData();

	while (od->size() > 0) {
		Buffer* b = od->front();
		channel->send(tag, b, FLAG_REPLY + (od->size() == 0 ? FLAG_FINAL : 0), this);
		od->pop_front();
	}
	channel->unregisterHandler(this);
}


void Handler::dataSent(Buffer* buf) {
	delete buf;
}
