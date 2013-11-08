package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]

set client_ptr [CoasterSWIGClientCreate $loop_ptr 127.0.0.1:53001]

set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1"]
