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


#include "ChannelConfigurationCommand.h"
#include <sstream>
#include "Logger.h"

using namespace Coaster;

using std::string;
using std::stringstream;

const string ChannelConfigurationCommand::NAME("CHANNELCONFIG");
const string ChannelConfigurationCommand::EMPTY("");
const string ChannelConfigurationCommand::KEEPALIVE("keepalive(-1)");

static int seq = 1;

ChannelConfigurationCommand::ChannelConfigurationCommand(): Command(&NAME) {
	stringstream ss;
	ss << "channel-";
	ss << seq++;
	localId = ss.str();
}

void ChannelConfigurationCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	serialize();
	Command::send(channel, cb);
}

void ChannelConfigurationCommand::serialize() {
	// use constant strings to avoid memory management issues
	addOutData(Buffer::wrap(KEEPALIVE));
	addOutData(Buffer::wrap(EMPTY)); // callback URL
	addOutData(Buffer::copy(localId));
	addOutData(Buffer::wrap(EMPTY)); // remoteId
}

void ChannelConfigurationCommand::replyReceived() {
	getInData()->at(0)->str(remoteId);
	LogInfo << "Channel id: " << localId << "-" << remoteId << endl;
	Command::replyReceived();
}
