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
