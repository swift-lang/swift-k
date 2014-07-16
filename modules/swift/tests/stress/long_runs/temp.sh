#!/bin/bash

for i in `ls *setup.sh`
do
    BASE=${i%.setup.sh};
    echo $BASE
    echo "Source file : $BASE.source.sh" 

    cat <<'EOF' >> $BASE.setup.sh

if [[ -n "$BEAGLE_USERNAME" ]]
then
    echo "BEAGLE_USERNAME is $BEAGLE_USERNAME";
    sed "s/{env.USER}/$BEAGLE_USERNAME/g" sites.template.xml > sites.backup && mv sites.backup sites.template.xml
fi;

EOF

done;