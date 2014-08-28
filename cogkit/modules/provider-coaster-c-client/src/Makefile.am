bin_PROGRAMS = coaster-client-test run-coaster-job
coaster_client_test_SOURCES = CoasterClientTest.cpp
run_coaster_job_SOURCES = RunCoasterJob.cpp
coaster_client_test_LDADD = libcoasterclient.la
run_coaster_job_LDADD = libcoasterclient.la

lib_LTLIBRARIES = libcoasterclient.la


#if SWIG

#SWIG_SRC = CoasterSWIG_wrap.cxx CoasterSWIG.cpp

#CoasterSWIG_wrap.cxx: CoasterSWIG.h
#	swig -c++ -importall $<
#	sed -i s/Coaster_Init/_Coasterclient_Init/ $@

#clean-local: 
#	rm -f CoasterSWIG_wrap.cxx

#endif

libcoasterclient_la_SOURCES = \
	$(SWIG_SRC) \
        coaster.cpp coaster.h coaster-defs.h \
	Lock.h Lock.cpp \
	ConditionVariable.h ConditionVariable.cpp \
	CommandCallback.h \
	CmdCBCV.h CmdCBCV.cpp \
	Buffer.h Buffer.cpp \
	RemoteCoasterException.h RemoteCoasterException.cpp \
	SerUtil.h \
	Logger.h Logger.cpp \
	Settings.h Settings.cpp \
	HandlerFactory.h HandlerFactory.cpp \
	ClientHandlerFactory.h ClientHandlerFactory.cpp \
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
	ChannelConfigurationCommand.h ChannelConfigurationCommand.cpp \
	ServiceConfigurationCommand.h ServiceConfigurationCommand.cpp \
	StagingSetEntry.h StagingSetEntry.cpp \
	CoasterError.h CoasterError.cpp \
	JobStatusHandler.h JobStatusHandler.cpp \
	RemoteLogHandler.h RemoteLogHandler.cpp \
	BQPStatusHandler.h BQPStatusHandler.cpp \
	ResourceUpdateHandler.h ResourceUpdateHandler.cpp \
	HeartBeatCommand.h HeartBeatCommand.cpp \
	ShutdownCommand.h ShutdownCommand.cpp
