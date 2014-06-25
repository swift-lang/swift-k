/*
 * ChannelCallback.h
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#ifndef CHANNELCALLBACK_H_
#define CHANNELCALLBACK_H_

#include "Buffer.h"

namespace Coaster {

class ChannelCallback {
	public:
		ChannelCallback();
		virtual ~ChannelCallback();
		virtual void dataSent(Buffer* buf) = 0;
};

class DeleteBufferCallback : public ChannelCallback {
	public:
		static DeleteBufferCallback CALLBACK;
		virtual void dataSent(Buffer* buf);
};

}

#endif /* CHANNELCALLBACK_H_ */
