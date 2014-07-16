/*
 * RequestReply.h
 *
 *  Created on: Aug 30, 2012
 *      Author: mike
 */

#ifndef REQUESTREPLY_H_
#define REQUESTREPLY_H_

#include "ChannelCallback.h"
#include "Buffer.h"
#include "Flags.h"
#include <vector>
#include <list>

namespace Coaster {

class CoasterChannel;

class RequestReply: public ChannelCallback {
	private:
		CoasterChannel* channel;
		std::list<Buffer*> outData;
		std::vector<Buffer*> inData;
		std::vector<Buffer*>* errorData;

		void clearBufferVector(std::vector<Buffer*>* v);
		
                /* Disable default copy constructor for this and subclasses */
		RequestReply(const RequestReply&);
		/* Disable default assignment for this and subclasses */
		RequestReply& operator=(const RequestReply&);

	protected:
		virtual void addOutData(Buffer*);
		virtual void addInData(Buffer*);
		virtual void addErrorData(Buffer*);

		virtual std::list<Buffer*>* getOutData();
		virtual std::vector<Buffer*>* getErrorData();
		virtual std::vector<Buffer*>* getInData();

		int getInDataAsInt(int index);
		long getInDataAsLong(int index);
		void getInDataAsString(int index, std::string& dest);

		std::string getErrorString();

		int tag;
	public:
		RequestReply();
		virtual ~RequestReply();

		void setChannel(CoasterChannel* pchannel);
		CoasterChannel* getChannel();

		void setTag(int ptag);
		int getTag();

		virtual void dataReceived(Buffer*, int flags);
		virtual void signalReceived(Buffer*);
		virtual void receiveCompleted(int flags) = 0;
};

}

#endif /* REQUESTREPLY_H_ */
