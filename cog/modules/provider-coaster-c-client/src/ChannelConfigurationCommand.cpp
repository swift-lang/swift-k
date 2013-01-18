#include "ChannelConfigurationCommand.h"
#include <sstream>
#include "Logger.h"

using namespace std;

string ChannelConfigurationCommand::NAME("CHANNELCONFIG");

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
	addOutData(Buffer::wrap("keepalive(-1)"));
	addOutData(Buffer::wrap("")); // callback URL
	addOutData(Buffer::copy(localId));
	addOutData(Buffer::wrap("")); // remoteId
}

void ChannelConfigurationCommand::replyReceived() {
	remoteId.append(*getInData()->at(0)->str());
	LogInfo << "Channel id: " << localId << "-" << remoteId << endl;
	Command::replyReceived();
}
