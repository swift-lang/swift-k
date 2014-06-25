#ifndef JOB_SUBMIT_COMMAND_H_
#define JOB_SUBMIT_COMMAND_H_

#include "Command.h"
#include "CommandCallback.h"
#include "Job.h"
#include <string>
#include "Buffer.h"

class JobSubmitCommand: public Command {
	private:
		Job* job;
		std::string ss;
	public:
		static std::string NAME;
		JobSubmitCommand(Job* job);
		virtual void send(CoasterChannel* channel, CommandCallback* cb);
		Job* getJob();
		std::string getRemoteId();
	private:
		void serialize();
};

#endif
