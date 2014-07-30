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


#ifndef CHANNEL_CONFIGURATION_COMMAND_H_
#define CHANNEL_CONFIGURATION_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include <string>
#include "Buffer.h"

namespace Coaster {

class ChannelConfigurationCommand: public Command {
	private:
		std::string localId;
		std::string remoteId;
	public:
		static const std::string NAME;
                static const std::string EMPTY;
                static const std::string KEEPALIVE;
		ChannelConfigurationCommand();
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void replyReceived();
	private:
		void serialize();
};

}

#endif
