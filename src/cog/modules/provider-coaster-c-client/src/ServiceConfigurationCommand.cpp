#include "ServiceConfigurationCommand.h"
#include <map>

using namespace std;

string ServiceConfigurationCommand::NAME("CONFIGSERVICE");

ServiceConfigurationCommand::ServiceConfigurationCommand(Settings& s): Command(&NAME) {
	settings = &s;
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
