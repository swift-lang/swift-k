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
