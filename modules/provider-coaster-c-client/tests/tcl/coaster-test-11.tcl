package require coaster 0.0

set loop_ptr [CoasterSWIGLoopCreate]
set client_ptr [CoasterSWIGClientCreate $loop_ptr 127.0.0.1:53001]
set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1,JOBS_PER_NODE=2"]
puts "Error code from CoasterSWIGClientSettings $x"

# Job stuff
set job1 [CoasterSWIGJobCreate "/bin/bash" ""]

puts "Job cmd set"
set stdout_dst "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stdout"
set stderr_dst "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/stderr"
set arg_script "/homes/yadunand/swift-trunk/cog/modules/provider-coaster-c-client/tests/tcl/wrapper.sh"

set rcode [CoasterSWIGJobSettings $job1 "" $arg_script "" "" \
               $stdout_dst $stderr_dst]

set rcode [CoasterSWIGSubmitJob $client_ptr $job1]
puts "Job1 submitted"

# Anything less that 100ms seems to cause a segfault.
# Maybe the buffers that are used aren't initialised till that wait time.?
# Why ?
after 100
while 1 {
    set rcode [CoasterSWIGGetJobStatus $client_ptr $job1]
    if { $rcode == 5 } {
        puts "Job1 failed"
        break
    } elseif { $rcode == 7 } {
        puts "Job1 completed successfully"
        break
    } else {
        puts "Rcode  : $rcode"
        after 5000
    }
}

set rcode [CoasterSWIGClientDestroy $client_ptr]

set rcode [CoasterSWIGLoopDestroy $loop_ptr]