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

namespace Coaster {

class HeartBeatCommand: public Command {
	private:
		long sendtime;
	public:
		static std::string NAME;
		HeartBeatCommand();
		virtual ~HeartBeatCommand();
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		virtual void dataSent(Buffer* buf);
		virtual void replyReceived();
};

}

#endif /* HEARTBEATCOMMAND_H_ */
