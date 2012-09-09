/*
 * command.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef COMMAND_H_
#define COMMAND_H_

#include <list>
#include <string>
#include "RequestReply.h"
#include "CoasterChannel.h"
#include "Flags.h"
#include "CommandCallback.h"

using namespace std;

class CoasterChannel;
class CommandCallback;

class Command: public RequestReply {
	private:
		const string* name;
		CommandCallback* cb;
	public:
		Command(const string* pname);

		const string* getName();

		virtual void send(CoasterChannel* channel);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);

		virtual void receiveCompleted(int flags);

		virtual void errorReceived();
		virtual void replyReceived();

		virtual void dataSent(Buffer* buf);

		friend ostream& operator<< (ostream& os, Command* cmd);
};


#endif /* COMMAND_H_ */
