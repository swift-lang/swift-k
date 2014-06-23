ACLOCAL_AMFLAGS = -I m4
SUBDIRS = src 
EXTRA_DIST = autogen.sh
LIBS = -pthread $(LIBS)
include_HEADERS = src/coasters.h src/coaster-defs.h \
            src/Buffer.h src/ChannelCallback.h \
            src/ChannelConfigurationCommand.h src/ClientHandlerFactory.h \
            src/CoasterChannel.h src/CoasterClient.h \
            src/CoasterError.h src/CoasterLoop.h src/CoasterSWIG.h \
            src/CommandCallback.h src/Command.h src/ConditionVariable.h \
            src/Flags.h src/HandlerFactory.h src/Handler.h \
            src/HeartBeatCommand.h src/Job.h src/JobStatus.h \
            src/JobStatusHandler.h src/JobSubmitCommand.h src/Lock.h \
            src/Logger.h src/RemoteCoasterException.h src/RequestReply.h \
            src/ServiceConfigurationCommand.h src/Settings.h \
            src/StagingSetEntry.h

AM_CXXFLAGS = -pthread

binaries: all
	mkdir -p dist/bin
	mkdir -p dist/lib
	cp src/.libs/libcoasterclient.a dist/lib
	cp -d src/.libs/libcoasterclient.so* dist/lib
	cp src/.libs/coaster-client-test dist/bin
	cp src/.libs/run-coaster-job dist/bin

if SWIG

SUBDIRS += tcl

endif
