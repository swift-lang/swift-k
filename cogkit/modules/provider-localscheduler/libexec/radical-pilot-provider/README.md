# aimes.swiftrp
Swift connector for radical.pilot


Swift interfaces with the execution provider via three calls :
      - (jobid)  stsubmit (taskinfo via stdin) 
      - (exitcode) stcancel (jobid)
      - (R|Q|C) ststat (jobid)
       
Note: Each submission is a separate task, no batching is possible here.


TODOs and potential issues:

1. Daemonize the radical pilot server script with thrift
..* Test with stubs
..* Handle the daemon at end of run
..* Test with concurrent accesses, concerns on whether the RP code is thread-safe

2. Calls to daemon from stsubmit/stcancel/ststat

3. Handle transfer of the _swiftwrap
..* This should just be hardcoded for now.

4. Handle transfer of data.
..* With staging method direct, the files to stage are explicitly provided, this can be handled over to RP for staging.

5. Walltimes.
..* Pilots have walltimes, compute units 