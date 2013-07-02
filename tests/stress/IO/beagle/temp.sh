#!/bin/bash

for i in `ls *setup.sh`
do
    BASE=${i%.setup.sh};
    echo $BASE
    echo "Source file : $BASE.source.sh" 

    cat <<'EOF' > $BASE.source.sh
#!/bin/bash

if [ "midway001" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;

EOF

done;