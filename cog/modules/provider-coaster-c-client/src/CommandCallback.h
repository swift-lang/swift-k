/*
 * CommandCallback.h
 *
 *  Created on: Sep 7, 2012
 *      Author: mike
 */

#ifndef COMMANDCALLBACK_H_
#define COMMANDCALLBACK_H_

#include <string>
#include "Command.h"

class Command;

using namespace std;

class CommandCallback {
	public:
		virtual void errorReceived(Command* cmd, string* message, string* details) = 0;
		virtual void replyReceived(Command* cmd) = 0;
};


#endif /* COMMANDCALLBACK_H_ */
