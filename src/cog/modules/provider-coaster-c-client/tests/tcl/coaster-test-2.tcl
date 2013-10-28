load ../../src/.libs/libcoasterclient.so

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr "140.221.8.81:40568"]


