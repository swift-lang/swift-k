load ../../src/.libs/libcoasterclient.so

set client_ptr [CoasterSWIGClientCreate 140.221.8.81:40568]
set x [CoasterSWIGClientSettings $client_ptr "SLOTS=1,MAX_NODES=1"]
puts $x
#CoasterSWIGTest "SLOTS=1,MAX_NODES=1"

