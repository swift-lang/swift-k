
bin_PROGRAMS = CoasterClientTest
CoasterClientTest_SOURCES = CoasterClientTest.cpp
CoasterClientTest_LDADD = libcoasterclient.la

lib_LTLIBRARIES = libcoasterclient.la

libcoasterclient_la_SOURCES = Lock.h Lock.cpp \
	ConditionVariable.h ConditionVariable.cpp \
	CommandCallback.h \
	Buffer.h Buffer.cpp \
	SerUtil.h \
	Logger.h Logger.cpp \
	HandlerFactory.h HandlerFactory.cpp \
	ChannelCallback.h ChannelCallback.cpp \
	CoasterCannel.h CoasterChannel.cpp \
	CoasterClient.h CoasterClient.cpp \
	CoasterLoop.h CoasterLoop.cpp \
	RequestReply.h RequestReply.cpp \
	Command.h Command.cpp \
	Handler.h Handler.cpp \
	JobStatus.h JobStatus.cpp \
	Job.h Job.cpp \
	JobSubmitCommand.h JobSubmitCommand.cpp \
	StagingSetEntry.h StagingSetEntry.cpp \
	CoasterError.h CoasterError.cpp \
	JobStatusHandler.h JobStatusHandler.cpp \
	HeartBeatCommand.h HeartBeatCommand.cpp
