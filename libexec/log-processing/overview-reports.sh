#!/bin/bash

echo "<html>"
echo "<head>"
echo "<title>overview of logfile runs</title>"
echo "</head>"
echo "<body>"
for a in $(find . -type d -name report-\* -not -name report-swift); do 

echo "<ul>"

#get runtime
read start duration rest < $a/workflow.event


echo "<li><a href=\"$a/index.html\">$a</a> - runtime $duration seconds</li>"
echo "</ul>"

duration=UNAVAILABLE

done
echo "</body>"

echo "</html>"

