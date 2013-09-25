#!/bin/bash

if [ -d "swift" ]
then
    rm -rf swift swift.tar    
fi

SITES_PASSED=`ls out/*out | wc -l`
HOME=$PWD
MAIL="$HOME/Mail.content"

cd out;
echo "Test-Results"                         >  $MAIL
echo "=====================================">> $MAIL
echo "Test home         : $PWD"             >> $MAIL
echo "Sites successful  : $SITES_PASSED"    >> $MAIL
VERSION=`swift -version | cut -d " " -f -2 | sed 's/\ /-/g' | tr '[A-Z]' '[a-z]'`
for i in `ls *tar`
do
    scp $i ci:~/public_html/$VERSION/
    tar -xf $i
    folder=`tar -tf $i | head -n 1`
    echo "Results for site : $folder"              >> $MAIL
    grep "Tests run" $folder/run*/tests*html       >> $MAIL
    grep "Tests succeeded" $folder/run*/tests*html >> $MAIL
    grep "Tests failed" $folder/run*/tests*html    >> $MAIL
    grep "Total Time" $folder/run*/tests*html      >> $MAIL
    echo ""
    rm -rf $folder
done
cd $HOME;
cd ..;
RUN_DIR=$(basename $PWD);
cd ..;
mv $RUN_DIR "$RUN_DIR-local"
tar -cf Local.tar "$RUN_DIR-local"
scp Local.tar ci:~/public_html/$VERSION/

echo "Running update and maintenance script on publish server";
ssh ci "cd ~/public_html/$VERSION/; ./maint.sh"
mv "$RUN_DIR-local" $RUN_DIR
rm Local.tar

echo "=====================================">> $MAIL
cd $HOME;
cat $MAIL
mailx -s "Test results from $HOSTNAME" -r $FROM_MAIL $TO_MAIL  < $MAIL

