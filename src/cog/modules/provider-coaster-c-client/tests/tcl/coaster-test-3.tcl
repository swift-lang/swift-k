load ../../src/.libs/libcoasterclient.so

set loop_ptr [CoasterSWIGLoopCreate]

set client_ptr [CoasterSWIGClientCreate $loop_ptr 140.221.8.81:40568]

set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1"]
