#ifndef SERVICE_CONFIGURATION_COMMAND_H_
#define SERVICE_CONFIGURATION_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include <string>
#include "Buffer.h"
#include "Settings.h"

namespace Coaster {

class ServiceConfigurationCommand: public Command {
	private:
		Settings* settings;
		std::string* configId;
	public:
		static std::string NAME;
                // TODO: how long does this hold a reference to settings?
		ServiceConfigurationCommand(Settings& s);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void replyReceived();
		std::string* getConfigId();
	private:
		void serialize();
};

}

#endif
