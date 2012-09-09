/*
 * coaster-channel.cpp
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include "CoasterChannel.h"
#include "CoasterError.h"
#include "HeartBeatCommand.h"
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>

#include <sstream>

using namespace std;

int socksend(int fd, const char* buf, int len, int flags);

class HeartBeatCommand;

DataChunk::DataChunk() {
	buf = NULL;
	cb = NULL;
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

CoasterChannel::CoasterChannel(CoasterClient* pClient, CoasterLoop* pLoop, HandlerFactory* pHandlerFactory) {
	sockFD = 0;
	handlerFactory = pHandlerFactory;
	tagSeq = rand() % 65536;
	loop = pLoop;
	client = pClient;

	readState = READ_STATE_IDLE;

	whdr.buf = new DynamicBuffer(HEADER_LENGTH);
	rhdr.buf = new DynamicBuffer(HEADER_LENGTH);
}

CoasterChannel::~CoasterChannel() {
	delete whdr.buf;
	delete rhdr.buf;
}

ostream& operator<< (ostream& os, CoasterChannel* channel) {
	os << "Channel[" << channel->getClient()->getURL() << "]";
	return os;
}


void CoasterChannel::start() {
	if (sockFD == 0) {
		throw CoasterError("channel start() called but no socket set");
	}
}

void CoasterChannel::shutdown() {

}

int CoasterChannel::getSockFD() {
	return sockFD;
}

void CoasterChannel::setSockFD(int psockFD) {
	sockFD = psockFD;
}

void CoasterChannel::read() {
	switch(readState) {
		case READ_STATE_IDLE:
			break;
		case READ_STATE_HDR:
			if (read(&rhdr)) {
				// full header read
				msg.reset();
				decodeHeader(&rtag, &rflags, &rlen);
				msg.buf = new DynamicBuffer(rlen);
				readState = READ_STATE_DATA;
			}
			else {
				break;
			}
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
	}
}

void CoasterChannel::dispatchData() {
	if (rflags & FLAG_REPLY != 0) {
		dispatchReply();
	}
	else {
		dispatchRequest();
	}
}

void CoasterChannel::dispatchReply() {
	if (commands.count(rtag) == 0) {
		throw new CoasterError("Received reply to unknown command (tag: %d)", rtag);
	}
	Command* cmd = commands[rtag];
	if (rflags & FLAG_SIGNAL != 0) {
		try {
			cmd->signalReceived(msg.buf);
		}
		catch (exception &e) {
			Logger::singleton() << endl;
			LogWarn << endl;
			LogWarn << "Command::signalReceived() threw exception: " << e.what() << endl;
		}
		catch (...) {
			LogWarn << "Command::signalReceived() threw unknown exception" << endl;
		}
	}
	else {
		cmd->dataReceived(msg.buf, rflags);
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
		string* name = msg.buf->str();
		Handler* h = handlerFactory->newInstance(name);
		if (h == NULL) {
			LogWarn << "Unknown handler: " << name << endl;
		}
		else {
			registerHandler(rtag, h);
		}
		delete name;
	}
	else {
		Handler* h = handlers[rtag];
		if (rflags & FLAG_SIGNAL != 0) {
			try {
				h->signalReceived(msg.buf);
			}
			catch (exception &e) {
				LogWarn << "Handler::signalReceived() threw exception: " << e.what() << endl;
			}
			catch (...) {
				LogWarn << "Handler::signalReceived() threw unknown exception" << endl;
			}
		}
		else {
			h->dataReceived(msg.buf, rflags);
			if (rflags & FLAG_FINAL) {
				try{
					h->receiveCompleted(rflags);
				}
				catch (exception &e) {
					LogWarn << "Handler::receiveCompleted() threw exception: " << e.what() << endl;
				}
				catch (...) {
					LogWarn << "Handler::receiveCompleted() threw unknown exception" << endl;
				}
			}
		}
	}
}

bool CoasterChannel::write() { Lock::Scoped l(writeLock);
	DataChunk* dc = sendQueue.front();

	Buffer* buf = dc->buf;

	int ret = socksend(sockFD, buf->getData(), (buf->getLen() - dc->bufpos), MSG_DONTWAIT);
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
		}
		return true;
	}
}

void CoasterChannel::makeHeader(int tag, Buffer* buf, int flags) {
	whdr.reset();
	Buffer* hdr = whdr.buf;
	hdr->putInt(0, tag);
	hdr->putInt(4, flags);
	hdr->putInt(8, buf->getLen());
	hdr->putInt(12, tag ^ flags ^ buf->getLen());
	hdr->putInt(16, 0);
}

void CoasterChannel::decodeHeader(int* tag, int* flags, int* len) {
	Buffer* buf = rhdr.buf;
	*tag = buf->getInt(0);
	*flags = buf->getInt(4);
	*len = buf->getInt(8);
	int hcsum = buf->getInt(12);
	int ccsum = *tag ^ *flags ^ *len;
	if (hcsum != ccsum) {
		throw CoasterError("Header checksum failed. Received checksum: %d, computed checksum: %d", hcsum, ccsum);
	}
}

void CoasterChannel::registerCommand(Command* cmd) {
	int tag = tagSeq++;
	cmd->setTag(tag);
	commands[tag] = cmd;
}

void CoasterChannel::unregisterCommand(Command* cmd) {
	commands.erase(cmd->getTag());
}

void CoasterChannel::registerHandler(int tag, Handler* h) {
	h->setTag(tag);
	handlers[tag] = h;
}

void CoasterChannel::unregisterHandler(Handler* h) {
	handlers.erase(h->getTag());
}


void CoasterChannel::send(int tag, Buffer* buf, int flags, ChannelCallback* cb) { Lock::Scoped l(writeLock);
	makeHeader(tag, buf, flags);
	sendQueue.push_back(&whdr);
	sendQueue.push_back(new DataChunk(buf, cb));
	loop->requestWrite(this);
}

CoasterClient* CoasterChannel::getClient() {
	return client;
}

void CoasterChannel::checkHeartbeat() {
	Command* cmd = new HeartBeatCommand();
	cmd->send(this);
}

void CoasterChannel::errorReceived(Command* cmd, string* message, string* details) {
	delete cmd;
	LogWarn << "Heartbeat failed: " << message << endl;
}

void CoasterChannel::replyReceived(Command* cmd) {
	delete cmd;
}

/*
 * Without this, GCC gets confused by CoasterChannel::send.
 */
int socksend(int fd, const char* buf, int len, int flags) {
	return send(fd, buf, len, flags);
}
