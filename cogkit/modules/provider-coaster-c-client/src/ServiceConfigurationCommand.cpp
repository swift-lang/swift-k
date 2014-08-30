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


#include "ServiceConfigurationCommand.h"
#include <map>

using namespace Coaster;

using std::map;
using std::string;

string ServiceConfigurationCommand::NAME("CONFIGSERVICE");

ServiceConfigurationCommand::ServiceConfigurationCommand(Settings& s): Command(&NAME) {
	settings = &s;
        configIdReceived = false;
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
	getInData()->at(0)->str(configId);
        configIdReceived = true;
	Command::replyReceived();
}

const std::string* ServiceConfigurationCommand::getConfigId() {
	if (configIdReceived) {
		return &configId;
        } else {
		return NULL;
        }
}
