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
 * HeartBeatCommand.cpp
 *
 *  Created on: Sep 5, 2012
 *      Author: mike
 */

#include "HeartBeatCommand.h"
#include "Logger.h"

#include <cassert>
#include <sstream>

using namespace Coaster;

using std::string;

string HeartBeatCommand::NAME("HEARTBEAT");

HeartBeatCommand::HeartBeatCommand(): Command(&NAME) {
}

HeartBeatCommand::~HeartBeatCommand() {
}

void HeartBeatCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	assert(channel != NULL);
	timeval now;

	gettimeofday(&now, NULL);
	sendtime = now.tv_sec * 1000 + now.tv_usec / 1000;
	addOutData(Buffer::wrap(sendtime));

	Command::send(channel, cb);
}

void HeartBeatCommand::dataSent(Buffer* buf) {
	delete buf;
}

void HeartBeatCommand::replyReceived() {
	long rectime = getInData()->at(0)->getLong(0);
        assert(getChannel() != NULL);
	LogInfo << "Latency for " << getChannel() << ": " << (rectime - sendtime) << endl;
	Command::replyReceived();
}
