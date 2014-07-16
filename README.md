This is the source code for the Swift parallel scripting language.
See http://swift-lang.org for more information.

Building
========
Run `ant redist` to build Swift.  The compiled Swift distribution will
be created under `dist/swift-<version>`.

Repository Layout
=================
The source code is divided up into modules.  Each module lives in
its own directory under `modules`, for example `modules/swift` or
`modules/provider-coaster`.

Some of the more interesting modules are:

* **swift**: the Swift language compiler and standard library
* **karajan**: the Karajan language, used as a compilation target for Swift.
* **provider-coaster**: the Coaster service and Java client for lightweight
    execution of jobs on remote resources
* **provider-coaster-c-client**: a C/C++ client for the Coaster service
* **provider-...**: other providers that support remote job execution on
    various computing resources




CoG Project
===========
This distribution of Swift incorporates code developed as part of the
CoG (Commodity Grid) project.  For information about the CoG project
and its licensing, see COG-README.txt, COG-CHANGES.txt and COG-LICENSE.txt.
