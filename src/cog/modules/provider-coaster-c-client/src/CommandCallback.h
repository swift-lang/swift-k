/*
 * CommandCallback.h
 *
 *  Created on: Sep 7, 2012
 *      Author: mike
 */

#ifndef COMMANDCALLBACK_H_
#define COMMANDCALLBACK_H_

#include <string>
#include "RemoteCoasterException.h"

namespace Coaster {

class Command;

class CommandCallback {
	public:
		virtual void errorReceived(Command* cmd, std::string* message, RemoteCoasterException* details) = 0;
		virtual void replyReceived(Command* cmd) = 0;
};

}

#endif /* COMMANDCALLBACK_H_ */
