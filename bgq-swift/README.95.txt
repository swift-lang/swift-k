Running Sub-block jobs via Swift
=================================

Introduction
------------

Argonne Leadership Computing Facility (ALCF) hosts a leadership class (among
the most powerful in the world) supercomputer, \textit{Mira}, which is a
50K-node IBM Blue Gene/Q (BG/Q) with peak performance of 10 PetaFLOPS.

Many applications are originally developed with small- and medium- size
execution on regular clusters  in mind. Some of these applications are later
used at large-scale in a workflow and/or many-task computing (MTC) style.  (MTC
is an emerging computation style wherein the computation consists of a large
number of medium-sized, semi-dependent tasks implemented as ordinary programs.)

The BG/Q resource manager, Cobalt, provides a mechanism
to run multiple small jobs (sub-block jobs) repeatedly over a larger outer
block, but in order to run an application in this mode, the user has to
manually determine the optimal geometry (i.e., select a subset of the nodes based on
their interconnection, allowing for best internode communication) and related
low-level parameters of the system. The subjob technique addresses this
challenge and enables MTC application to be run on the BG/Q.  

This document describes the subjob approach to run multiple jobs over a single
large block of compute nodes on Blue Gene/Q systems. The technique lets users
submit multiple, independent, repeated jobs within a single larger Cobalt
block. 

The Swift-subjob package provides tools, scripts and example use-cases to run
Swift applications in subjob mode over the ALCF BG/Q resources: +Vesta+,
+Cetus+ and +Mira+. The framework is flexible in that the same Swift script and
configuration can be used to run subjob or non-subjob mode depending on the
scale and size of a run. Users can run multiple 'waves' of jobs asynchronously
and in parallel. 

Swift subjobs
--------------

To download the package, checkout the directory as follows:

----
svn co https://svn.ci.uchicago.edu/svn/vdl2/SwiftApps/subjobs
----

followed by:

----
cd  subjobs
----

To set up the environment:

----
source setup
----

To run an example application

----
./runswift.sh
----

How To
-------

To convert an ordinary Swift application run in sub-block mode, the following changes are required:

First, Add bg.sh as the application invoker in place of +sh+ or any other invoker. For example, if the app definition is as follows:

----
sh @exe @i @o arg("s","1") stdout=@sout stderr=@serr;
----

Replace the shell invocation with the bg.sh invocations like so:

----
bg.sh @exe @i @o arg("s","1") stdout=@sout stderr=@serr;
----

Second, add the +SUBBLOCK_SIZE+ environment variable to the sites file. For example:

----
<profile key="SUBBLOCK_SIZE" namespace="env">16</profile>
----

NOTE: The value of +SUBBLOCK_SIZE+ variable must be a power of 2 greater than 8 and less than the +maxnodes+ value.

////
A complete example sites file for a sub-block job run on ALCF +Vesta+ is shown below:

----
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.ci.uchicago.edu/swift/SwiftSites">

<pool handle="cluster">
<execution provider="coaster" jobmanager="local:cobalt" />

<!-- "slots" determine the number of Cobalt jobs -->
<profile namespace="globus" key="slots">1</profile>
<profile namespace="globus" key="mode">script</profile>

<profile namespace="karajan" key="jobThrottle">2.99</profile>
<profile namespace="karajan" key="initialScore">10000</profile>
<profile namespace="globus" key="maxwalltime">00:40:00</profile>
<profile namespace="globus" key="walltime">2050</profile>
<profile namespace="globus" key="maxnodes">256</profile>
<profile namespace="globus" key="nodegranularity">256</profile>

<!-- required for sub-block jobs, remove for non-sub-block jobs -->
<profile key="SUBBLOCK_SIZE" namespace="env">16</profile>
<profile namespace="globus" key="jobsPerNode">16</profile>
 
<workdirectory>/tmp/swiftwork</workdirectory>
<filesystem provider="local"/>

</pool>
</config>
----

Of note are the +SUBBLOCK_SIZE+ and the +mode+ properties which must be present
in the sites definition. The former defines the size of the subblock needed and
the latter specifies that the "mode" to run the outer cobalt job would be
+script+ mode. In this particular example, we have the outer block size to be
256 nodes whereas the subblock size is 16 nodes. This results in a total of 16
subblocks resulting in +jobsPerNode+ value to be 16.

////

NOTE: Swift installation for sub-block jobs on Vesta and Mira machines can be
found at +/home/ketan/swift-0.95/cog/modules/swift/dist/swift-svn/bin/swift+

Use-Case Applications
----------------------

This section discusses some of the real-world use-case applications that are set up
with this package. These applications are tested with subblock and non-subblock
runs on ALCF Vesta, a 2-rack (2048 nodes) BlueGene/Q system.

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

ToDo: Visualize NAMD results with VMD.

Rosetta
~~~~~~~~

+Rosetta+ is a molecular http://rosie.rosettacommons.org/docking[docking]
toolkit with many related programs used by many large-scale HPC science
applications. This implementation shows how to run +FlexPeptide Docking+ on
ALCF systems. The Swift scripts, configuration and application specific inputs
are found in the +rosetta+ directory. To run the example:

----
cd rosetta
./runvesta.sh #run on vesta
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
bgsh "/home/ketan/openmp-gnu-july16-mini/build/src/debug/linux/2.6/64/ppc64/xlc/static-mpi/FlexPepDocking.staticmpi.linuxxlcdebug" "-database" "/home/ketan/minirosetta_database" "-pep_refine" "-s" @pdb_file "-ex1" "-ex2aro" "-use_input_sc" "-nstruct" nstruct "-overwrite" "-scorefile" @_scorefile stdout=@out stderr=@err;
----

Dock 
~~~~~

Dock is another molecular docking
http://dock.compbio.ucsf.edu/DOCK_6/dock6_manual.htm[program] used by many
applications. The Swift source, configuration and application related inputs
can be found in the +dock+ directory. To run the Dock example:

----
cd dock
./runvesta.sh #run on vesta
./runmira.sh #run on mira
----

GridPack
~~~~~~~~
GridPack is a simulation package designed for powergrid design application.

HALO
~~~~
HALO is a astrophysics application.


Internals
----------
The key driver of the Swift sub-block jobs is a script called +bg.sh+ that does
the sub-block jobs calculations and othe chores for the users. The script looks
as follows:

----
include::bg.sh[]
----

Sub-block limitations
----------------------

There are currently the following two limitations with sub-block jobs:

. The maximum size of the outer Cobalt job must not exceed 512 nodes, ie. half
of a hardware rack.

. The successive subjobs must be submitted with a gap of at least 3 seconds in
between. This means for a large number of shorter than 5 seconds jobs, the
system will be underutilized. Consequently, subblocks are suitable for
tasks which are more than a couple of minutes in duration.

Further Information
--------------------

. More information about Swift can be found http://swift-lang.org/main[here].
. More about ALCF can be found http://www.alcf.anl.gov[here].
. More about IBM BlueGene sub-block jobs can be found http://www.alcf.anl.gov/files/ensemble_jobs_0.pdf[here] (warning: pdf).

