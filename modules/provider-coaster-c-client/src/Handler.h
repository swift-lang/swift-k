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

		virtual void dataSent(Buffer* buf);
};

}

#endif /* HANDLER_H_ */
