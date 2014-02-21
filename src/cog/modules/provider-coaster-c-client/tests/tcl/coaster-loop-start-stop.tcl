load ../../src/.libs/libcoasterclient.so

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr 127.0.0.1:53001]

set rcode [CoasterSWIGClientDestroy $client_ptr]
set rcode [CoasterSWIGLoopDestroy $loop_ptr]




