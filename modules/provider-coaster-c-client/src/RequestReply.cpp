/*
 * RequestReply.cpp
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#include "RequestReply.h"
#include "CoasterError.h"

using namespace Coaster;

#include <sstream>

using std::list;
using std::stringstream;
using std::string;
using std::vector;

RequestReply::RequestReply() {
	channel = NULL;
	errorData = NULL;
}

RequestReply::~RequestReply() {
	clearBufferVector(&inData);
	while (outData.size() > 0) {
		delete outData.front();
		outData.pop_front();
	}
	clearBufferVector(errorData);
	delete errorData;
}

void RequestReply::clearBufferVector(vector<Buffer*>* v) {
	if (v == NULL) {
		return;
	}

	vector<Buffer*>::iterator i;

	for (i = v->begin(); i != v->end(); i++) {
		delete *i;
	}
}

void RequestReply::setChannel(CoasterChannel* pchannel) {
	channel = pchannel;
}

CoasterChannel* RequestReply::getChannel() {
	return channel;
}

void RequestReply::setTag(int ptag) {
	tag = ptag;
}

int RequestReply::getTag() {
	return tag;
}

void RequestReply::addOutData(Buffer* buf) {
	outData.push_back(buf);
}

void RequestReply::addInData(Buffer* buf) {
	inData.push_back(buf);
}

void RequestReply::getInDataAsString(int index, string& dest) {
	Buffer* buf = getInData()->at(index);
	dest.assign(buf->getData(), buf->getLen());
}

int RequestReply::getInDataAsInt(int index) {
	return getInData()->at(index)->getInt(0);
}

long RequestReply::getInDataAsLong(int index) {
	return getInData()->at(index)->getLong(0);
}

void RequestReply::addErrorData(Buffer* buf) {
	if (errorData == NULL) {
		errorData = new vector<Buffer*>;
	}
	errorData->push_back(buf);
}

list<Buffer*>* RequestReply::getOutData() {
	return &outData;
}

vector<Buffer*>* RequestReply::getInData() {
	return &inData;
}

vector<Buffer*>* RequestReply::getErrorData() {
	return errorData;
}

/*
 * Received data from channel.
 * NOTE: we now own buf!
 */
void RequestReply::dataReceived(Buffer* buf, int flags) {
	if (flags & FLAG_ERROR) {
		addErrorData(buf);
	}
	else {
		addInData(buf);
	}
}

void RequestReply::signalReceived(Buffer* buf) {
	throw CoasterError("Unhandled signal: " + string(buf->getData(), buf->getLen()));
}

string RequestReply::getErrorString() {
	if (errorData == NULL) {
		return "";
	}
	else {
		stringstream ss;
		vector<Buffer*>::iterator it;

		for (it = errorData->begin(); it != errorData->end(); it++) {
			ss << (*it) << '\n';
		}
		return ss.str();
	}
}
