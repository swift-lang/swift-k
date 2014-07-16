#!/bin/bash

for i in `ls *setup.sh`
do
    BASE=${i%.setup.sh};
    echo $BASE
    echo "Source file : $BASE.source.sh" 

    cat <<'EOF' > $BASE.check.sh
#!/bin/bash                                                                                                                                            
echo "Cleaning up"
rm -rf "dummy" driver*.out &> /dev/null

EOF

done;