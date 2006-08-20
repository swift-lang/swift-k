#!/bin/bash

# Use this script to delete Eclipse related files from the cog
# Must be run from the cog directory
# This is unsupported

exclude="template qos portlets"
rm -rf .build
rm -f .project
rm -f .classpath
for module in modules/*; do
	rm -rf $module/.build
	rm -f $module/.project
	rm -f $module/.classpath
done
