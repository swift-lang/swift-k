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

// 1 minute
#define HEARTBEAT_CHECK_INTERVAL 60

using namespace std;

class CoasterLoop {
	private:
		pthread_t thread;

		map<int, CoasterChannel*> channelMap;
		list<CoasterChannel*> addList;
		list<CoasterChannel*> removeList;

		int wakePipe[2];
		fd_set rfds, wfds;
		int socketCount;
		int maxFD;

		void updateMaxFD();
		void acknowledgeWake();

		time_t lastHeartbeatCheck;
	public:
		bool started;
		bool done;
		Lock lock;

		CoasterLoop();
		virtual ~CoasterLoop();
		void start();
		void stop();

		void addChannel(CoasterChannel* channel);
		void removeChannel(CoasterChannel* channel);
		void addSockets();
		void removeSockets();
		void awake();
		fd_set* getReadFDs();
		fd_set* getWriteFDs();
		int getMaxFD();
		bool readSockets(fd_set* fds);
		void writeSockets(fd_set* fds);
		int getWakePipeReadFD();

		void requestWrite(CoasterChannel* channel);

		void checkHeartbeats();
};

#endif /* COASTER_LOOP_H_ */
