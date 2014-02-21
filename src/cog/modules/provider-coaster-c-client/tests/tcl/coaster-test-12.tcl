package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr 127.0.0.1:53001]
set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1,JOBS_PER_NODE=2"]
puts "Error code from CoasterSWIGClientSettings $x"

# Job stuff
set job1 [CoasterSWIGJobCreate "/bin/bash" ""]
set job2 [CoasterSWIGJobCreate "/bin/bash" "fork"]
puts "Job cmd set"
set stdout_dst1 "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stdout1"
set stderr_dst1 "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stderr1"
set stdout_dst2 "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stdout2"
set stderr_dst2 "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stderr2"
set arg_script "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/wrapper.sh"

set rcode [CoasterSWIGJobSettings $job1 "" $arg_script "" "" \
               $stdout_dst1 $stderr_dst1]
set rcode [CoasterSWIGJobSettings $job2 "" $arg_script "" "" \
               $stdout_dst2 $stderr_dst2]


set rcode [CoasterSWIGSubmitJob $client_ptr $job1]
set rcode [CoasterSWIGSubmitJob $client_ptr $job2]

# Anything less that 100ms seems to cause a segfault.
# Maybe the buffers that are used aren't initialised till that wait time.?
# Why ?
after 100
while 1 {
    set rcode1 [CoasterSWIGGetJobStatus $client_ptr $job1]
    set rcode2 [CoasterSWIGGetJobStatus $client_ptr $job2]
    if { $rcode1 == 7 && $rcode2 == 7 } {
        puts "Job1 and Job2 completed"
        break
    } else {
        puts "Job1 : $rcode1 , Job2 : $rcode2"
        after 2000
    }
}

set rcode [CoasterSWIGClientDestroy $client_ptr]

set rcode [CoasterSWIGLoopDestroy $loop_ptr]