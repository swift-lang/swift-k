#ifndef CHANNEL_CONFIGURATION_COMMAND_H_
#define CHANNEL_CONFIGURATION_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include <string>
#include "Buffer.h"

using namespace std;

class ChannelConfigurationCommand: public Command {
	private:
		string localId;
		string remoteId;
	public:
		static string NAME;
		ChannelConfigurationCommand();
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void replyReceived();
	private:
		void serialize();
};

#endif
