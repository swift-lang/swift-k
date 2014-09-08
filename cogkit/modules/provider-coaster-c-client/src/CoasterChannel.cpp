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


/*
 * coaster-channel.cpp
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include "CoasterChannel.h"
#include "CoasterError.h"
#include "HeartBeatCommand.h"
#include <cassert>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include "Logger.h"

#include <algorithm>
#include <cassert>
#include <sstream>

using namespace Coaster;

using std::exception;
using std::list;
using std::map;
using std::min;
using std::string;

static int socksend(int fd, const char* buf, int len, int flags);
static void pp(char* dest, const char* src, int len);

class HeartBeatCommand;

DataChunk::DataChunk() {
	bufpos = 0;
}

DataChunk::DataChunk(Buffer* pbuf, ChannelCallback* pcb) {
	buf = pbuf;
	cb = pcb;
	bufpos = 0;
}

void DataChunk::reset() {
	bufpos = 0;
}

void HeartbeatCallback::errorReceived(Command* cmd, string* message, RemoteCoasterException* details) {
	delete cmd;
	LogWarn << "Heartbeat failed: " << message << endl;
}

void HeartbeatCallback::replyReceived(Command* cmd) {
	delete cmd;
}

CoasterChannel::CoasterChannel(CoasterClient* client, CoasterLoop* loop,
			       HandlerFactory* handlerFactory) :
			       rhdr_buf(HEADER_LENGTH),
			       rhdr(&rhdr_buf, (ChannelCallback*)NULL) {
	assert(loop != NULL);
	assert(client != NULL);
	sockFD = 0;
	this->handlerFactory = handlerFactory;
	tagSeq = rand() % 65536;
	this->loop = loop;
	this->client = client;

	readState = READ_STATE_HDR;
	msg.buf = NULL;
}

CoasterChannel::~CoasterChannel() {
	for (map<int, Handler*>::iterator it = handlers.begin();
	     it != handlers.end(); ++it) {
		delete it->second;
	}

}

void CoasterChannel::start() {
	if (sockFD == 0) {
		throw CoasterError("channel start() called but no socket set");
	}
}

void CoasterChannel::close() {
	shutdown(sockFD, 2);
}

int CoasterChannel::getSockFD() {
	return sockFD;
}

void CoasterChannel::setSockFD(int psockFD) {
	sockFD = psockFD;
}

void CoasterChannel::read() {
	switch(readState) {
		case READ_STATE_HDR:
			if (read(&rhdr)) {
				// full header read
				decodeHeader(&rtag, &rflags, &rlen);
				// setup message buffer for rest of message
				msg.reset();
				// Buffer should be cleared
				assert(msg.buf == NULL);
				msg.buf = new DynamicBuffer(rlen);
				readState = READ_STATE_DATA;
			}
			else {
				break;
			}
			/* no break */
		case READ_STATE_DATA:
			if (read(&msg)) {
				// full message read
				dispatchData();
				rhdr.reset();
				readState = READ_STATE_HDR;
			}
			break;
	}
}

bool CoasterChannel::read(DataChunk* dc) {
	int ret = recv(sockFD, dc->buf->getModifiableData(), (dc->buf->getLen() - dc->bufpos), MSG_DONTWAIT);
	if (ret == -1) {
		if (errno == EAGAIN || errno == EWOULDBLOCK) {
			return false;
		}
		else {
			throw CoasterError("Socket read error: %s", strerror(errno));
		}
	}
	else {
		dc->bufpos += ret;
		if (dc->bufpos == dc->buf->getLen()) {
			return true;
		}
		else {
			return false;
		}
	}
}

void CoasterChannel::dispatchData() {
	if (rflags & FLAG_REPLY) {
		dispatchReply();
	}
	else {
		dispatchRequest();
	}
}

Command* CoasterChannel::getRegisteredCommand(int tag) { Lock::Scoped l(writeLock);
	if (commands.count(rtag) == 0) {
		return NULL;
	}
	else {
		return commands[rtag];
	}
}

void CoasterChannel::dispatchReply() {
	Command* cmd = getRegisteredCommand(rtag);
	if (cmd == NULL) {
		throw new CoasterError("Received reply to unknown command (tag: %d)", rtag);
	}
	LogDebug << "dispatching reply " << rtag << ", " << rflags << endl;
	if (rflags & FLAG_SIGNAL) {
		try {
			cmd->signalReceived(msg.detachBuffer());
		}
		catch (exception &e) {
			LogWarn << "Command::signalReceived() threw exception: " << e.what() << endl;
		}
		catch (...) {
			LogWarn << "Command::signalReceived() threw unknown exception" << endl;
		}
	}
	else {
		cmd->dataReceived(msg.detachBuffer(), rflags);
		if (rflags & FLAG_FINAL) {
			unregisterCommand(cmd);
			try {
				cmd->receiveCompleted(rflags);
			}
			catch (exception &e) {
				LogWarn << "Command::receiveCompleted() threw exception: " << e.what() << endl;
			}
			catch (...) {
				LogWarn << "Command::receiveCompleted() threw unknown exception" << endl;
			}
		}
	}
}

void CoasterChannel::dispatchRequest() {
	if (handlers.count(rtag) == 0) {
		// initial request
		string name;
		msg.buf->str(name);

		// Done with data
		msg.deleteBuffer();

		LogDebug << "Handling initial request for " << name << endl;
		Handler* h = handlerFactory->newInstance(name);
		if (h == NULL) {
			LogWarn << "Unknown handler: " << name << endl;
		}
		else {
			registerHandler(rtag, h);
		}
	}
	else {
		Handler* h = handlers[rtag];
		if (rflags & FLAG_SIGNAL) {
			try {
				h->signalReceived(msg.detachBuffer());
			}
			catch (exception &e) {
				LogWarn << "Handler::signalReceived() threw exception: " << e.what() << endl;
			}
			catch (...) {
				LogWarn << "Handler::signalReceived() threw unknown exception" << endl;
			}
			unregisterHandler(h);
		}
		else {
			h->dataReceived(msg.detachBuffer(), rflags);

			if (rflags & FLAG_FINAL) {
				try {
					h->receiveCompleted(rflags);
				}
				catch (exception &e) {
					LogWarn << "Handler::receiveCompleted() threw exception: " << e.what() << endl;
				}
				catch (...) {
					LogWarn << "Handler::receiveCompleted() threw unknown exception" << endl;
				}
				unregisterHandler(h);
			}
		}
	}
}

/**
 * Attempts to write the data chunk at the front of the queue. If the entire chunk is
 * written, it removes it from the queue and returns true. Returns false there are bytes
 * left to write in the chunk
 */
bool CoasterChannel::write() { Lock::Scoped l(writeLock);
	DataChunk* dc = sendQueue.front();
	
	Buffer* buf = dc->buf;

	int ret = socksend(sockFD, buf->getData(), (buf->getLen() - dc->bufpos), MSG_DONTWAIT);
	char tmp[81];
	pp(tmp, buf->getData(), min(80, ret));
	LogDebug << this << " sent " << ret << " bytes: " << tmp << endl;
	if (ret == -1) {
		if (errno == EAGAIN || errno == EWOULDBLOCK) {
			return false;
		}
		else {
			throw CoasterError("Socket write error: %s", strerror(errno));
		}
	}
	else {
		dc->bufpos += ret;
		if (dc->bufpos == buf->getLen()) {
			sendQueue.pop_front();
			if (dc->cb != NULL) {
				try {
					dc->cb->dataSent(buf);
				}
				catch (exception &e) {
					LogWarn << "Callback threw exception: " << e.what() << endl;
				}
				catch (...) {
					LogWarn << "Callback threw unknown exception" << endl;
				}
			}
			delete dc;
			return true;
		}
		else {
			return false;
		}
	}
}

DataChunk* CoasterChannel::makeHeader(int tag, Buffer* buf, int flags) {
	DynamicBuffer *hdrbuf = new DynamicBuffer(HEADER_LENGTH);
	DataChunk* whdr = new DataChunk(hdrbuf, &DeleteBufferCallback::CALLBACK);
	Buffer* hdr = whdr->buf;
	hdr->putInt(0, tag);
	hdr->putInt(4, flags);
	hdr->putInt(8, buf->getLen());
	int hcsum = tag ^ flags ^ buf->getLen();
	hdr->putInt(12, hcsum);
	hdr->putInt(16, 0);
	char tmp[128];
	sprintf(tmp, "Send[tag: 0x%08x, flags: 0x%08x, len: 0x%08d, hcsum: 0x%08x]", tag, flags, buf->getLen(), hcsum);
	LogDebug << tmp << endl;
	return whdr;
}

void CoasterChannel::decodeHeader(int* tag, int* flags, int* len) {
	Buffer* buf = rhdr.buf;
	*tag = buf->getInt(0);
	*flags = buf->getInt(4);
	*len = buf->getInt(8);
	int hcsum = buf->getInt(12);
	int ccsum = *tag ^ *flags ^ *len;
	char tmp[128];
	sprintf(tmp, "Recv[tag: 0x%08x, flags: 0x%08x, len: 0x%08d, hcsum: 0x%08x]", *tag, *flags, *len, hcsum);
	LogDebug << tmp << endl;
	if (hcsum != ccsum) {
		throw CoasterError("Header checksum failed. Received checksum: %d, computed checksum: %d", hcsum, ccsum);
	}
}

void CoasterChannel::registerCommand(Command* cmd) { Lock::Scoped l(writeLock);
	int tag = tagSeq++;
	cmd->setTag(tag);
	commands[tag] = cmd;
}

void CoasterChannel::unregisterCommand(Command* cmd) { Lock::Scoped l(writeLock);
	commands.erase(cmd->getTag());
}

void CoasterChannel::registerHandler(int tag, Handler* h) {
	h->setTag(tag);
	handlers[tag] = h;
	h->setChannel(this);
}

void CoasterChannel::unregisterHandler(Handler* h) {
	handlers.erase(h->getTag());
	delete h;
}


void CoasterChannel::send(int tag, Buffer* buf, int flags, ChannelCallback* cb) { Lock::Scoped l(writeLock);
	assert(buf != NULL);
	assert(buf->getData() != NULL);
	sendQueue.push_back(makeHeader(tag, buf, flags));
	sendQueue.push_back(new DataChunk(buf, cb));
	loop->requestWrite(this, 2);
}

CoasterClient* CoasterChannel::getClient() {
	return client;
}

void CoasterChannel::checkHeartbeat() {
	// TODO: this can be sent after shutdown
	Command* cmd = new HeartBeatCommand();
	cmd->send(this, &heartbeatCB);
}

const string& CoasterChannel::getURL() const {
	return client->getURL();
}

/*
 * Without this, GCC gets confused by CoasterChannel::send.
 */
static int socksend(int fd, const char* buf, int len, int flags) {
	return send(fd, buf, len, flags);
}

static void pp(char* dest, const char* src, int len) {
	for(int i = 0; i < len; i++) {
		char c = src[i];
		if (c < '0' || c > 127) {
			c = '.';
		}
		dest[i] = c;
	}
	dest[len] = 0;
}
