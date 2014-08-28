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
#include <utility>
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
		std::list<std::pair<CoasterChannel*, bool> > removeList;

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

		/*
		 * Stop the coaster loop.  Returns once stopped.
		 * All channels should be removed (e.g. by stopping clients)
		 * before stopping the loop.
		 */
		void stop();
		
		/*
		 * Add a channel for the loop to monitor.
		 * Ownership of the channel is shared between caller and the loop.
		 * Must be removed later by a call to removeChannel().
		 * Loop must be started before adding channel
		 */
		void addChannel(CoasterChannel* channel);

		/*
		 * Schedule removal of channel from loop.
		 * The removal is performed by the loop thread
		 * sometime after the call.
		 * deleteChan: if true, the channel will be deleted after
                        removal and the channel socket fd will be closed
		 */
		void removeChannel(CoasterChannel* channel, bool deleteChan);
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
