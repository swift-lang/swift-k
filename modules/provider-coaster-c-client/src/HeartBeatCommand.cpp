/*
 * HeartBeatCommand.cpp
 *
 *  Created on: Sep 5, 2012
 *      Author: mike
 */

#include "HeartBeatCommand.h"
#include "Logger.h"
#include <sstream>

using namespace Coaster;

using std::string;

string HeartBeatCommand::NAME("HEARTBEAT");

HeartBeatCommand::HeartBeatCommand(): Command(&NAME) {
}

HeartBeatCommand::~HeartBeatCommand() {
}

void HeartBeatCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	timeval now;

	gettimeofday(&now, NULL);
	sendtime = now.tv_sec * 1000 + now.tv_usec / 1000;
	addOutData(new StaticBuffer(sendtime));

	Command::send(channel, cb);
}

void HeartBeatCommand::dataSent(Buffer* buf) {
	delete buf;
}

void HeartBeatCommand::replyReceived() {
	long rectime = getInData()->at(0)->getLong(0);

	LogInfo << "Latency for " << getChannel() << ": " << (rectime - sendtime) << endl;
	Command::replyReceived();
}
