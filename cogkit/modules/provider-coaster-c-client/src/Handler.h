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
 * handler.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef HANDLER_H_
#define HANDLER_H_

#include "CoasterChannel.h"
#include "RequestReply.h"

namespace Coaster {

class CoasterChannel;

class Handler: public RequestReply {
	protected:
		virtual void sendReply(std::string& reply);
		virtual void sendReply(const char* reply);

	public:
		virtual void receiveCompleted(int flags);

		virtual void errorReceived();
		virtual void requestReceived();

		virtual void send(CoasterChannel* channel);
};

}

#endif /* HANDLER_H_ */
