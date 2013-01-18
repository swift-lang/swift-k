/*
 * ChannelCallback.h
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#ifndef CHANNELCALLBACK_H_
#define CHANNELCALLBACK_H_

#include "Buffer.h"

class ChannelCallback {
	public:
		ChannelCallback();
		virtual ~ChannelCallback();
		virtual void dataSent(Buffer* buf) = 0;
};

#endif /* CHANNELCALLBACK_H_ */
