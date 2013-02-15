Copy all files in a new directory, edit the Makefile and change variables as needed,
then run one of the following targets:

release          makes a full release without publishing

maven 			 builds a maven repository of abstractions

publish          publishes a release on the release host

lock             makes all release files in the release directory on the
                 release host read-only

cog              generates all cog packages

cog-no-checkout  like cog, but it expects a checkout to exist in ./src/cog

guides           generates all guides

web              generates the web-pages

clean            cleans all generated packages

