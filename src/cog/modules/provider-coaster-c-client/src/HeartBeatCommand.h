/*
 * HeartBeatCommand.h
 *
 *  Created on: Sep 5, 2012
 *      Author: mike
 */

#ifndef HEARTBEATCOMMAND_H_
#define HEARTBEATCOMMAND_H_

#include "Command.h"
#include <sys/time.h>
#include <string>

using namespace std;

class HeartBeatCommand: public Command {
	private:
		long sendtime;
	public:
		static string NAME;
		HeartBeatCommand();
		virtual ~HeartBeatCommand();
		void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void dataSent(Buffer* buf);
		virtual void replyReceived();
};

#endif /* HEARTBEATCOMMAND_H_ */
