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
