#ifndef SERVICE_CONFIGURATION_COMMAND_H_
#define SERVICE_CONFIGURATION_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include <string>
#include "Buffer.h"
#include "Settings.h"

using namespace std;

class ServiceConfigurationCommand: public Command {
	private:
		Settings* settings;
	public:
		static string NAME;
                // TODO: how long does this hold a reference to settings?
		ServiceConfigurationCommand(Settings& s);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
	private:
		void serialize();
};

#endif
