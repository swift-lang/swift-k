/*
 * CmdCBCV.h
 *
 *  Created on: Sep 9, 2012
 *      Author: mike
 */

#ifndef CMDCBCV_H_
#define CMDCBCV_H_

#include "CommandCallback.h"
#include "Lock.h"
#include "ConditionVariable.h"
#include "RemoteCoasterException.h"

namespace Coaster {

class CmdCBCV: public CommandCallback {
	private:
		bool done;
		ConditionVariable cv;
		Lock lock;
	public:
		CmdCBCV();
		virtual ~CmdCBCV();
		virtual void errorReceived(Command* cmd, std::string* message, RemoteCoasterException* details);
		virtual void replyReceived(Command* cmd);
		void wait();
};

}
#endif /* CMDCBCV_H_ */
