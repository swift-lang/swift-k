/*
 * CmdCBCV.cpp
 *
 *  Created on: Sep 9, 2012
 *      Author: mike
 */

#include "CmdCBCV.h"

using namespace Coaster;

using std::string;

CmdCBCV::CmdCBCV() {
	done = false;
}

CmdCBCV::~CmdCBCV() {
}

void CmdCBCV::errorReceived(Command* cmd, string* message, RemoteCoasterException* details) {
	replyReceived(cmd);
}

void CmdCBCV::replyReceived(Command* cmd) { Lock::Scoped l(lock);
	done = true;
	cv.broadcast();
}

void CmdCBCV::wait() { Lock::Scoped l(lock);
	while(!done) {
		cv.wait(lock);
	}
}
