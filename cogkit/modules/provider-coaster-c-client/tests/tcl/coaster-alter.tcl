load ../../src/.libs/libcoasterclient.so

set loop [CoasterSWIGLoopMake]
set client [CoasterSWIGClientMake $loop 140.221.8.81:40568]
set res [CoasterSWIGClientSettingsMake $client "SLOTS=1,MAX_NODES=1"]


