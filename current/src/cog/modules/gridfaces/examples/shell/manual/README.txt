=====================================================
README.txt
=====================================================

These are the examples from the manual modified to specifically run on wiggum (you must change the connection.gsh file to change the machines you will connect to). Before running these examples please go through the init steps.

-----------------------------------------------------
Init Steps:
-----------------------------------------------------
1) modify gnet.gsh to set to your desired ip address
2) modify connection.gsh to specify the machines you will be connecting to.
3) compile input/go_world.c on the remote machine in your home directory (make sure the object file is goWorld ie gcc -o goWorld go_world.c)
4) open gridshell and then change the this directory

HINT: I use the file examplepath.gsh placed in my root directory to change the path quickly. 
1) You will need to modify "/argonne/checkouts/current/src/cog/modules/gridfaces/examples/shell/" to reflect where you place the examples. 
2) Copy examplepath.gsh to your root directory
3) In the shell type "gsh /examplepath.gsh"
4) You are now in the correct directory

-----------------------------------------------------
Files:
-----------------------------------------------------
ex-1.gsh ... ex-7.gsh 
  examples found in the manual
gnet.gsh 
  specifies the ipaddress used for i/o redirection
connection.gsh 
  specifies the machines you will connect to
examplepath.gsh
  I use this file to change to the directory these examples are in (see Init Steps: HINT)
input
  directory containing all files used for sending to remote machines
output
  directory containing all files received from the remote machines
