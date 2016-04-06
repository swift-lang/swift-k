Running Sub-block jobs via Swift
=================================

Introduction
------------

The BG/Q resource manager, Cobalt, provides a mechanism to run multiple small
jobs (sub-block jobs) repeatedly over a larger outer block. In order to run an
application in this mode, the user must manually determine the optimal geometry
(i.e., select a subset of the nodes based on their interconnection, allowing
for best internode communication) and related low-level parameters of the
system. The subjob technique addresses this challenge and enables MTC
applications on the BG/Q. The technique lets users submit multiple,
independent, repeated jobs within a single larger Cobalt block. 

The Swift-subjob package provides tools, scripts and example use-cases to run
Swift applications in subjob mode over the ALCF BG/Q resources: +Cetus+ (for
small-scale testing) and +Mira+. The framework is flexible in that the same
configuration can be used to run subjob or non-subjob mode depending on the
scale and size of a run. Users can run multiple 'waves' of jobs asynchronously
and in parallel. 

Quickstart
-----------

Download the subjob demo package as follows:

----
wget http://mcs.anl.gov/~ketan/subjobs.tgz
----

followed by:

----
tar zxf subjobs.tgz
cd  subjobs/simanalyze/part05
----

To run the example application: 

----
./runcetus.sh #on cetus
#or
./runmira.sh #on mira
----

Another example is found in +subjobs/simanalyze/part06+

For the details about the working of this example, see Swift tutorial
http://swift-lang.org/tutorials/localhost/tutorial.html#_part_3_analyzing_results_of_a_parallel_ensemble[here].

Background
-----------

This section briefly discusses the Swift parallel scripting, Subjobs and their integration.

Swift
~~~~~~
The Swift parallel scripting framework eases the expression and execution of workflow and MTC applications such as ensembles and parameter sweeps.

Subjobs
~~~~~~~~
Subjobs offer a way to run ensembles and parameter sweep like computations over BG/Q resources.

Depending on the number of tasks, size of each job and total number of concurrent jobs, the underlying mechanism used in subjobs will vary. Essentially there are two ways subjobs are invoked:

. Prebooted sub-blocks
. Bootable sub-blocks

.Sub-blocks summary
[options="header"]
|============================================
|Total Nodes|Task Size      |Sub-blocks type
|<512       |<512           |Prebooted
|>512       |<512           |Mixed
|>512       |>512           |Bootable
|============================================

The commands to invoke sub-blocks on ALCF BG/Q systems:

. get-corners.py
. get-bootable-blocks
. boot-block
. runjob


Diving deep
------------

Convert any Swift script to subjob
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To configure a Swift application to run in subjob mode, the following
changes are required:

First, add +bg.sh+ as the application invoker in place of +sh+ or any other
invoker. For example, if the app definition is as follows:

----
sh @exe @i @o arg("s","1") stdout=@sout stderr=@serr;
----

Replace the invocation with the +bg.sh+ invocations like so:

----
bg.sh @exe @i @o arg("s","1") stdout=@sout stderr=@serr;
----

Second, export the +SUBBLOCK_SIZE+ environment variable. For example:

----
export SUBBLOCK_SIZE=16
----

NOTE: The value of +SUBBLOCK_SIZE+ variable must be a power of 2 and less than 512.

Restart a Swift run
~~~~~~~~~~~~~~~~~~~~

Swift can restart a run from the point it was interrupted or crashed. This can
be done by supplying a +-resume+ switch and the restart log found in the +run+
directory. For example, if a run directory named +run010+ is created as a
result of a run, it can be resumed as follows:

----
swift -resume ./run010/restart.log -config mira.conf simanalyze.swift
----

Swift Configuration
~~~~~~~~~~~~~~~~~~~~
A complete example config file for a sub-block job run on ALCF is shown below:

----
sites : cluster
site.cluster {
    execution {
        type: "coaster"
        URL: "localhost"
        jobManager: "local:cobalt"
        options {
            maxNodesPerJob: 32
            maxJobs: 1
            tasksPerNode: 2
            #workerLoggingLevel = "DEBUG"
            nodeGranularity: 32
            maxJobTime = "00:60:00"
        }
    }
    filesystem {
        type: "local"
        URL: "localhost"
    }
    staging : direct
    workDirectory: "/home/"${env.USER}"/swift.work"
    maxParallelTasks: 30
    initialParallelTasks: 29
    app.bgsh {
        executable: "/home/ketan/SwiftApps/subjobs/bg.sh"
        maxWallTime: "00:04:00"
        env.SUBBLOCK_SIZE="16"
    }
}

executionRetries: 0
keepSiteDir: true
providerStagingPinSwiftFiles: false
alwaysTransferWrapperLog: true
----

Of note are the +SUBBLOCK_SIZE+ property which must be present in the sites
definition. It defines the size of the subblock needed to run the script.  In
this particular example, we have the outer block size to be 256 nodes whereas
the subblock size is 16 nodes. This results in a total of 16 subblocks
resulting in +jobsPerNode+ value to be 16.

NOTE: Swift installation for sub-block jobs on Vesta and Mira machines can be
found at +/home/ketan/swift-k/dist/swift-svn/bin/swift+

Use-Case Applications
----------------------

This section discusses some of the real-world use-cases that are set up as demo
applications with this package. These applications are tested with subblock as
well as non-subblock runs on BG/Q system.

NAMD
~~~~

+NAMD+ is a molecular dynamics simulation code developed at
http://www.ks.uiuc.edu/Research/namd[uiuc]. The Swift source and configuration
files along with application inputs can be found in the +namd+ directory in the
subjobs pacjage. To run NAMD example:

----
cd namd #change to the package's namd directory
./runvesta.sh #run on vesta
./runmira.sh  #run on mira
----

To run NAMD with a different input dataset, change the input files in the
+input_files+ directory and reflect the changes in the +namd.swift+ source code,
especially the data definitions part:

----
file psf <"input_files/h0_solvion.psf">;
file pdb <"input_files/h0_solvion.pdb">;
file coord_restart <"input_files/h0_eq.0.restart.coor">;
file velocity_restart <"input_files/h0_eq.0.restart.vel">;
file system_restart <"input_files/h0_eq.0.restart.xsc">;
file namd_config <"input_files/h0_eq.conf">;
file charmm_parameters <"input_files/par_all22_prot.inp">;
----

Similarly, in order to change the scale and size of runs, make changes to the
parameters in the sites file as described in section 1 above.

Rosetta
~~~~~~~~

+Rosetta+ is a molecular http://rosie.rosettacommons.org/docking[docking]
toolkit with many related programs used by many large-scale HPC science
applications. This implementation shows how to run +FlexPeptide Docking+ on
ALCF systems. The Swift scripts, configuration and application specific inputs
are found in the +rosetta+ directory. To run the example:

----
cd rosetta
./runmira.sh #run on mira
----

To change scale, size and/or inputs of the run, change the location of input
files in the Swift source file (+rosetta.swift+) like so:

----
file pdb_files[] <filesys_mapper; location="hlac_complex", suffix=".pdb">;
----

In the above line all +.pdb+ files in the directory +hlac_complex+ will be
processed in parallel. Change the +location+ from +hlac_complex+ to where you
have your input files.

To change the number of generated +structs+ by Flexpep Docking program, change
the struct argument in the call to the rosetta application like so:

----
(scorefile, rosetta_output, rosetta_error) = rosetta(pdb, 2);
----

In the above line, the number +2+ indicates the number of +structs+ to be
generated by the docking. Change this value to the desired size to change the
desired number of +structs+. To make changes to other parameters, make changes
to the commandline as invoked in the +app+ definition in the Swift script like
so:

----
bgsh \
"/home/ketan/openmp-gnu-july16-mini/build/src/debug/linux/2.6/64/ppc64/xlc/static-mpi/FlexPepDocking.staticmpi.linuxxlcdebug" \
"-database" "/home/ketan/minirosetta_database" \
"-pep_refine" "-s" @pdb_file "-ex1" "-ex2aro" \
"-use_input_sc" "-nstruct" nstruct "-overwrite" \
"-scorefile" @_scorefile stdout=@out stderr=@err;
----

Dock 
~~~~~

Dock is another molecular docking
http://dock.compbio.ucsf.edu/DOCK_6/dock6_manual.htm[program] used by many
applications. The Swift source, configuration and application related inputs
can be found in the +dock+ directory. To run the Dock example:

----
cd dock
./runcetus.sh #run on cetus
./runmira.sh #run on mira
----

////
GridPack
~~~~~~~~
GridPack is a simulation package designed for powergrid design application.

HALO
~~~~
HALO is an astrophysics application.
////

Diving deeper
--------------
The key driver of the Swift sub-block jobs is a script called +bg.sh+ that does
the sub-block jobs calculations and othe chores for the users. The script looks
as follows:

----
include::bg.sh[]
----

////
Sub-block limitations
----------------------

There are currently the following two limitations with sub-block jobs:

. The maximum size of the outer Cobalt job must not exceed 512 nodes, ie. half
of a hardware rack.

. The successive subjobs must be submitted with a gap of at least 3 seconds in
between. This means for a large number of shorter than 5 seconds jobs, the
system will be underutilized. Consequently, subblocks are suitable for
tasks which are more than a couple of minutes in duration.
////

Further Information
--------------------

. More information about Swift can be found http://swift-lang.org/main[here].
. More about ALCF can be found http://www.alcf.anl.gov[here].
. More about IBM BlueGene sub-block jobs can be found http://www.alcf.anl.gov/files/ensemble_jobs_0.pdf[here] (PDF).

