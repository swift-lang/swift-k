#ifndef JOB_SUBMIT_COMMAND_H_
#define JOB_SUBMIT_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include "Job.h"
#include <vector>
#include <string>
#include "Buffer.h"

using namespace std;

class JobSubmitCommand: public Command {
	private:
		Job* job;
		string ss;
	public:
		static string NAME;
		JobSubmitCommand(Job* job);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		Job* getJob();
		string getRemoteId();
	private:
		void serialize();
};

#endif
