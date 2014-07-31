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


#ifndef JOB_SUBMIT_COMMAND_H_
#define JOB_SUBMIT_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include "Job.h"
#include <string>
#include "Buffer.h"

namespace Coaster {

class JobSubmitCommand: public Command {
	private:
		Job* job;
		std::string ss;
		std::string configId;
	public:
		static std::string NAME;
		JobSubmitCommand(Job* job, const std::string& configId);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		Job* getJob();
		std::string getRemoteId();
	private:
		void serialize();
};

}

#endif
