# Swift

Description and Overview

The Swift scripting language provides a simple, compact way to write parallel scripts that run many copies of ordinary programs concurrently in various workflow patterns, reducing the need for complex parallel programming or arcane scripting to achieve this common high-level task. Swift is very general, and is in use in domains ranging from earth systems to bioinformatics to molecular modeling.

Swift’s highly portable programming model is based on implicitly parallel functional dataflow. The same Swift script runs on multi-core computers, clusters, grids, clouds, and supercomputers, and is thus a useful tool for moving workflow computations from laptop to distributed and/or high performance systems. 

There are two versions of Swift: **Swift/K** and **Swift/T**. Both provide the high-level Swift programming model, coordinating the concurrent execution of many sequential tasks.

_Swift/K_ is the original Swift implementation, which runs as a workflow engine on a NERSC login node. It allocates compute node resources using a set of one or more pilot jobs across NERSC resources.  For example, this can be used to run a large number of  application invocations inside a small number of large PBS allocations. 

_Swift/T_ is an implementation of the Swift language for high-performance computing. In this implementation, the Swift script is translated into an MPI program for highly scalable dataflow processing over MPI, without single-node bottlenecks. Swift/T provides a programming model for much finer-grain in-memory workflow on extreme scale systems, where it can perform very fine-grained tasks at rates of millions per second when running on tens of thousands of nodes.


