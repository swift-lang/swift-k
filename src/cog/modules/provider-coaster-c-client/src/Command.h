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
#include "ConditionVariable.h"
#include "CommandCallback.h"
#include "RemoteCoasterException.h"

namespace Coaster {

class CoasterChannel;
class CommandCallback;


class Command: public RequestReply {
	private:
		const std::string* name;
		CommandCallback* cb;
		bool ferrorReceived, freceiveCompleted;
	public:
		Command(const std::string* pname);

		const std::string* getName();

		virtual void send(CoasterChannel* channel);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void execute(CoasterChannel* channel);

		virtual void receiveCompleted(int flags);

		virtual void errorReceived();
		virtual void replyReceived();

		virtual bool isReceiveCompleted() const;
		virtual bool isErrorReceived() const;
		virtual std::string* getErrorMessage();
		virtual RemoteCoasterException* getErrorDetail();

		virtual void dataSent(Buffer* buf);

		template<typename cls> friend cls& operator<< (cls& os, Command* cmd);
};

template<typename cls> cls& operator<< (cls& os, Command* cmd) {
	os << "Command[" << cmd->getName() << ", tag: " << cmd->getTag() << "]";
	return os;
}

}

#endif /* COMMAND_H_ */
