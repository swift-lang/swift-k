This is the source code for the Swift parallel scripting language.
See http://swift-lang.org for more information.

Building
========
Run `ant redist` to build Swift.  The compiled Swift distribution will
be created under `dist/swift-<version>` (dist/swift-svn for the
development version).

Repository Layout
=================
At the root of the repository are a number of subdirectories that contain
the source code and supporting files for the Swift language.  The main
directories are:

* **bin**: Swift executables
* **build**: working directory for temporary build files
* **cogkit**: modules based on the Java CoG kit
* **dist**: destination for final compiled distribution of Swift
* **docs**: Swift documentation
* **etc**: miscellaneous supporting files
* **lib**: libraries required for Swift, particularly Java jars.
* **libexec**: other libraries required for Swift, particularly Karajan
      libraries and shell scripts 
* **resources**: miscellaneous resources used in building Swift.
* **src**: Java source code for Swift compiler and runtime.
* **tests**: test suite for Swift

Swift depends on a number of additional modules that reside in their own
directories under `cogkit/modules`, for example `cogkit/modules/karajan` or
`cogkit/modules/provider-coaster`.

Some of the more interesting modules are:

* **karajan**: the Karajan language, used as a compilation target for Swift.
* **provider-coaster**: the Coaster service and Java client for lightweight
    execution of jobs on remote resources
* **provider-coaster-c-client**: a C/C++ client for the Coaster service
* **provider-...**: other providers that support remote job execution on
    various computing resources

Java CoG Kit
============
This distribution of Swift incorporates code developed as part of the
CoG (Commodity Grid) project.  For information about the Java CoG Kit
see README.txt, CHANGES.txt and LICENSE.txt in cogkit.
