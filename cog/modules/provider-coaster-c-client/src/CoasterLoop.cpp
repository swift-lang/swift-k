/*
 * coaster-loop.cpp
 *
 *  Created on: Jun 9, 2012
 *      Author: mike
 */

#include "CoasterLoop.h"
#include "CoasterError.h"
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>

void* run(void* ptr);
void checkSelectError(int ret);

CoasterLoop::CoasterLoop() {
	started = false;
	done = false;
	socketCount = 0;
	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
}

void CoasterLoop::start() { Lock::Scoped l(lock);
	if (started) {
		return;
	}

	time(&lastHeartbeatCheck);

	if (pipe2(wakePipe, O_NONBLOCK) != 0) {
			throw CoasterError(string("Could not create pipe: ") + strerror(errno));
	}
	FD_SET(wakePipe[0], &rfds);

	thread = pthread_create(&thread, NULL, run, this);

	started = 1;
}

void CoasterLoop::stop() { Lock::Scoped l(lock);

	if (!started) {
		return;
	}
	done = 1;
}

void* run(void* ptr) {
	struct timeval timeout;
	CoasterLoop* loop = (CoasterLoop*) ptr;
	fd_set myrfds, mywfds;
	fd_set* rfds = loop->getReadFDs();
	fd_set* wfds = loop->getWriteFDs();

	while(1) {
		{ Lock::Scoped l(loop->lock);
			if (loop->done) {
				loop->started = false;
				break;
			}
			loop->addSockets();
			loop->removeSockets();
		}

		timeout.tv_sec = 0.1;
		timeout.tv_usec = 0;

		// fd sets are updated by select, so make new ones every time
		memcpy(&myrfds, rfds, sizeof(myrfds));
		int ret = select(loop->getMaxFD() + 1, &myrfds, NULL, NULL, &timeout);

		checkSelectError(ret);

		// can read or has data to write
		// try to read from each socket
		if (!loop->readSockets(&myrfds)) {
			// no channel sockets had anything to read, so there is data to write
			{ Lock::Scoped l(loop->lock);
				// synchronize when copying wfds since they are concurrently updated
				// based on whether a channel has stuff to write or not
				memcpy(&mywfds, wfds, sizeof(mywfds));
			}

			timeout.tv_sec = 0;
			timeout.tv_usec = 0;
			// don't wait; just see if any sockets that need to be written to
			// can be written to
			int ret = select(loop->getMaxFD() + 1, NULL, &mywfds, NULL, &timeout);
			checkSelectError(ret);
			loop->writeSockets(&mywfds);
		}
		loop->checkHeartbeats();
	}

	return NULL;
}

bool CoasterLoop::readSockets(fd_set* fds) {
	map<int, CoasterChannel*>::iterator it;

	bool writePending = false;

	for (it = channelMap.begin(); it != channelMap.end(); ++it) {
		if (FD_ISSET(it->first, fds)) {
			if (it->first == getWakePipeReadFD()) {
				writePending = true;
			}
			else {
				it->second->read();
			}
		}
	}

	return writePending;
}

void CoasterLoop::writeSockets(fd_set* fds) {
	map<int, CoasterChannel*>::iterator it;

	for (it = channelMap.begin(); it != channelMap.end(); ++it) {
		if (FD_ISSET(it->first, fds)) {
			if (it->second->write()) {
				acknowledgeWake();
			}
		}
	}
}


void checkSelectError(int ret) {
	if (ret < 0) {
		if (errno == EBADF) {
			// at least one fd is invalid/has an error

		}
		else {
			throw CoasterError(string("Error in select: ") + strerror(errno));
		}
	}
}

void CoasterLoop::addSockets() {
	// must be called with lock held
	list<CoasterChannel*>::iterator i;
	for (i = addList.begin(); i != addList.end(); ++i) {
		int sockFD = (*i)->getSockFD();
		FD_SET(sockFD, &rfds);
		channelMap[sockFD] = *i;
		socketCount++;
	}

	updateMaxFD();

	addList.clear();
}

void CoasterLoop::removeSockets() {
	// must be called with lock held
	list<CoasterChannel*>::iterator i;
	for (i = removeList.begin(); i != removeList.end(); ++i) {
		int sockFD = (*i)->getSockFD();
		FD_CLR(sockFD, &rfds);
		channelMap.erase(sockFD);
		socketCount--;
	}

	updateMaxFD();

	removeList.clear();
}

void CoasterLoop::addChannel(CoasterChannel* channel) { Lock::Scoped l(lock);
	addList.push_back(channel);
}

void CoasterLoop::removeChannel(CoasterChannel* channel) { Lock::Scoped l(lock);
	removeList.push_back(channel);
}

void CoasterLoop::awake() {
	int result = write(wakePipe[1], "0", 1);
}

void CoasterLoop::acknowledgeWake() {
	char buf[1];
	int result = read(wakePipe[0], buf, 1);
}

fd_set* CoasterLoop::getReadFDs() {
	return &rfds;
}

fd_set* CoasterLoop::getWriteFDs() {
	return &wfds;
}

int CoasterLoop::getMaxFD() {
	return maxFD;
}

void CoasterLoop::updateMaxFD() {
	maxFD = getWakePipeReadFD();

	map<int, CoasterChannel*>::iterator it;
	for (it = channelMap.begin(); it != channelMap.end(); ++it) {
		if (maxFD < it->first) {
			maxFD = it->first;
		}
	}
}

int CoasterLoop::getWakePipeReadFD() {
	return wakePipe[0];
}

void CoasterLoop::requestWrite(CoasterChannel* channel) { Lock::Scoped l(lock);
	FD_SET(channel->getSockFD(), &wfds);
	updateMaxFD();
	awake();
}

void CoasterLoop::checkHeartbeats() {
	time_t now;

	time(&now);

	if (now - lastHeartbeatCheck > HEARTBEAT_CHECK_INTERVAL) {
		lastHeartbeatCheck = now;
		{ Lock::Scoped l(lock);
			map<int, CoasterChannel*>::iterator it;
			for (it = channelMap.begin(); it != channelMap.end(); ++it) {
				it->second->checkHeartbeat();
			}
		}
	}
}
