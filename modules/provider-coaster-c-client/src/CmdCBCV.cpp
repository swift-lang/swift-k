/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2012-2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
