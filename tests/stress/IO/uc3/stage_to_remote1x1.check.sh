
#!/bin/bash

echo "Cleaning up"
rm -rf "dummy" *.out &> /dev/null

if [ "$KILL_JAVA" == "true" ];
then
    echo "Killing Java.."
    for dead in `ps axf -u $USER | grep HeapDumpOnOutOfMemoryError | grep -o ^[0-9]*`
    do
        kill -9 $dead
    done
else
    echo "No"
fi

