package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]

set client_ptr [CoasterSWIGClientCreate $loop_ptr 140.221.8.81:58318]

set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1,JOBS_PER_NODE=2"]
puts "Error code from CoasterSWIGClientSettings "
puts $x

set job1 [CoasterSWIGJobCreate "/bin/hostname"]
puts "Job set to /bin/hostname"

set rcode [CoasterSWIGSubmitJob $client_ptr $job1]
puts "Job1 submitted"

puts "Waiting for Job1"
set rcode [CoasterSWIGWaitForJob $client_ptr $job1]
puts "Job1 complete"

set rcode [CoasterSWIGClientDestroy $client_ptr]

set rcode [CoasterSWIGLoopDestroy $loop_ptr]