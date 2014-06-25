/*
 * coaster-loop.h
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#ifndef COASTER_LOOP_H_
#define COASTER_LOOP_H_

#include "CoasterChannel.h"
#include "Lock.h"
#include <map>
#include <list>
#include <pthread.h>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <ctime>
#include <stdio.h>

namespace Coaster {

class CoasterLoop {
	private:
		pthread_t thread;

		std::map<int, CoasterChannel*> channelMap;
		std::list<CoasterChannel*> addList;
		std::list<CoasterChannel*> removeList;

		int wakePipe[2];
		fd_set rfds, wfds;
		int socketCount;
		int maxFD;
		int writesPending;

		void updateMaxFD();
		void acknowledgeWriteRequest(int count);

		time_t lastHeartbeatCheck;

		/* Disable default copy constructor */
		CoasterLoop(const CoasterLoop&);
		/* Disable default assignment */
		CoasterLoop& operator=(const CoasterLoop&);
	public:
		bool started;
		bool done;
		Lock lock;

		CoasterLoop();
		virtual ~CoasterLoop();
		void start();
		void stop();
		
		/*
		 * Add a channel for the loop to monitor.
		 * Ownership of the channel is retained by the caller.
		 * Must be removed later by a call to removeChannel().
		 */
		void addChannel(CoasterChannel* channel);
		void removeChannel(CoasterChannel* channel);
		void addSockets();
		void removeSockets();
		void requestWrite(int count);
		fd_set* getReadFDs();
		fd_set* getWriteFDs();
		int getMaxFD();
		bool readSockets(fd_set* fds);
		void writeSockets(fd_set* fds);
		int getWakePipeReadFD();

		void requestWrite(CoasterChannel* channel, int count);

		void checkHeartbeats();
};

}
#endif /* COASTER_LOOP_H_ */
