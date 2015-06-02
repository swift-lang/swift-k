# Swift

### Description and Overview

The Swift scripting language provides a simple, compact way to write parallel scripts that run many copies of ordinary programs concurrently in various workflow patterns, reducing the need for complex parallel programming or arcane scripting to achieve this common high-level task. Swift is very general, and is in use in domains ranging from earth systems to bioinformatics to molecular modeling.

Swift’s highly portable programming model is based on implicitly parallel functional dataflow. The same Swift script runs on multi-core computers, clusters, grids, clouds, and supercomputers, and is thus a useful tool for moving workflow computations from laptop to distributed and/or high performance systems. 

There are two versions of Swift: **Swift/K** and **Swift/T**. Both provide the high-level Swift programming model, coordinating the concurrent execution of many sequential tasks.

_Swift/K_ is the original Swift implementation, which runs as a workflow engine on a NERSC login node. It allocates compute node resources using a set of one or more pilot jobs across NERSC resources.  For example, this can be used to run a large number of  application invocations inside a small number of large PBS allocations. 

_Swift/T_ is an implementation of the Swift language for high-performance computing. In this implementation, the Swift script is translated into an MPI program for highly scalable dataflow processing over MPI, without single-node bottlenecks. Swift/T provides a programming model for much finer-grain in-memory workflow on extreme scale systems, where it can perform very fine-grained tasks at rates of millions per second when running on tens of thousands of nodes.

The names above derive from the underlying technology used: K for the Karajan workflow engine written as a Java package; T for the Turbine workflow engine implemented using the ADLB (Asynchronous Dynamic Load Balancing) MPI library.

#### When to use Swift/K vs Swift/T
Use Swift/T if:
* your tasks are short (seconds to subseconds)
* your workflow fits well into a single scheduler (e.g., PBS) job
* you can optionally link your application code against the Swift/T framework.
 
Use Swift/K if:
* your apps are longer-running (minutes to hours)
* your apps are complex executables that can not be readily linked into Swift/T
* your workflows have time-varying resource demands
* It is advantageous to break your resource requests into multiple smaller jobs

#### Access
Type the following commands to run a simple Swift/K script:
```
% module load swift
% swift -config swift.conf myscript.swift
```
Type the following commands to run a simple Swift/T script:
```
% module load swift
% swift-t -m "cray" myscript.swift
```
#### Using Swift on NERSC Systems

##### A simple Swift/K script
```
type file;

app (file o) cat (file i){
  cat @i stdout=@o;
}

file out[]<simple_mapper; location="outdir", prefix="f.",suffix=".out">;

foreach i in [0:9]{
  file data<"data.txt">;
  out[i] = cat(data);
}
```
Swift/K uses a configuration file to interface with compute resource. See the tutorial link below for details about this configuration file. Assuming a swift.conf file is setup for the run, the above script could be run from the command line as:

```
% swift -config swift.conf catsn.swift
```

##### A simple Swift/T script
```
import io;
printf("Hello world!");
```
Swift/T uses a shell wrapper provided as part of the installation to compile, generate and submit jobs to compute resources:
```
% swift-t -m cray hello.swift
```
### Documentation
The documentation for [Swift/K](http://swift-lang.org/docs/index.php) and [Swift/T](http://swift-lang.org/Swift-T/guide.html) is available on-line. For further support, subscribe to the user discussion list for [Swift](https://lists.ci.uchicago.edu/cgi-bin/mailman/listinfo/swift-user).

### Tutorial
A general hands-on tutorial for [Swift/K](http://swift-lang.org/swift-tutorial/doc/tutorial.html) and [Swift/T](http://swift-lang.org/Swift-T/turbine-sites.html#_edison) with site-specific examples for NERSC systems is available on-line.

### Availability
