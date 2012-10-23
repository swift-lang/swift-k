#!/bin/bash

# This script is referenced by asciidoc to point to the correct versions of documentation based on version information
# print_info.sh <guide>
# This will print the correct URL based on SVN version info

pushd ../.. > /dev/null 2>&1
VERSION=`svn info |grep URL|awk -F / '{print $NF}'`
popd > /dev/null 2>&1

# Parse command line arguments
case "$1" in
   userguide)
      echo http://www.ci.uchicago.edu/swift/guides/$VERSION/userguide/userguide.html[Swift User Guide]
      ;;
   tutorial)
      echo http://www.ci.uchicago.edu/swift/guides/$VERSION/tutorial/tutorial.html[Swift Tutorial]
      ;;
   *) echo http://www.ci.uchicago.edu/swift/docs/index.php[Swift Documentation] 
      ;;
esac
