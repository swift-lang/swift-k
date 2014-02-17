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

class CoasterChannel;

using namespace std;

class RequestReply: public ChannelCallback {
	private:
		CoasterChannel* channel;
		list<Buffer*> outData;
		vector<Buffer*> inData;
		vector<Buffer*>* errorData;

		void clearBufferVector(vector<Buffer*>* v);

	protected:
		virtual void addOutData(Buffer*);
		virtual void addInData(Buffer*);
		virtual void addErrorData(Buffer*);

		virtual list<Buffer*>* getOutData();
		virtual vector<Buffer*>* getErrorData();
		virtual vector<Buffer*>* getInData();

		int getInDataAsInt(int index);
		long getInDataAsLong(int index);
		void getInDataAsString(int index, string& dest);

		string getErrorString();

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

#endif /* REQUESTREPLY_H_ */
