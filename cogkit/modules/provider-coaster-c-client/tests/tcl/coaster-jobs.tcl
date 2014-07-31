load ../../src/.libs/libcoasterclient.so

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr 140.221.8.81:53001]

set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1"]

set job1_ptr [CoasterSWIGJobCreate "/bin/hostname" ""]

set returncode [CoasterSWIGSubmitJob $client_ptr $job1_ptr]

set rcode [CoasterSWIGClientDestroy $client_ptr]
set rcode [CoasterSWIGLoopDestroy $loop_ptr]
