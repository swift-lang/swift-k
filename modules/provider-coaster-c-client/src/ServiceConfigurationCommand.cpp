#include "ServiceConfigurationCommand.h"
#include <map>

using namespace Coaster;

using std::map;
using std::string;

string ServiceConfigurationCommand::NAME("CONFIGSERVICE");

ServiceConfigurationCommand::ServiceConfigurationCommand(Settings& s): Command(&NAME) {
	settings = &s;
	configId = NULL;
}

void ServiceConfigurationCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	serialize();
	Command::send(channel, cb);
}

void ServiceConfigurationCommand::serialize() {
	map<string, string>::iterator i;
	map<string, string>& m = settings->getSettings();

	for (i = m.begin(); i != m.end(); i++) {
		string ss;
		ss.append(i->first);
		ss.append("=");
		ss.append(i->second);
		addOutData(Buffer::copy(ss));
	}
}

void ServiceConfigurationCommand::replyReceived() {
	configId = getInData()->at(0)->str();
	Command::replyReceived();
}

std::string* ServiceConfigurationCommand::getConfigId() {
	return configId;
}
