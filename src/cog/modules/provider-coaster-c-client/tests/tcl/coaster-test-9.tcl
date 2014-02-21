package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr 127.0.0.1:53001]
set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1,JOBS_PER_NODE=2"]
puts "Error code from CoasterSWIGClientSettings $x"

# Job stuff
set job1 [CoasterSWIGJobCreate "/bin/echo" ""]
puts "Job set to /bin/hostname"
set rcode [CoasterSWIGJobSettings $job1 "" "hello world how are you \\\$USERNAME \\\$RANDOM \\\$HOSTNAME" "" "" \
                                  "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stdout" \
                                  "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stderr" ]

set rcode [CoasterSWIGSubmitJob $client_ptr $job1]
puts "Job1 submitted"

puts "Waiting for Job1"
set rcode [CoasterSWIGWaitForJob $client_ptr $job1]
puts "Job1 complete"

set rcode [CoasterSWIGClientDestroy $client_ptr]

set rcode [CoasterSWIGLoopDestroy $loop_ptr]