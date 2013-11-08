package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr "127.0.0.1:53001"]


