#!/bin/bash

PUBLISH_LOCATION=/scratch/midway/yadunand/publish
#PUBLISH_SERVER=http://swift.rcc.uchicago.edu:8042
PUBLISH_SERVER=http://users.rcc.uchicago.edu/~yadunand

DATE=$(date +%H%M%S)

[ ! -z $FROM_MAIL ] || FROM_MAIL="test-engine@swift.rcc.uchicago"
#[ ! -z $TO_MAIL ]   || TO_MAIL="yadudoc1729@gmail.com"

if [ -d "swift" ]
then
    rm -rf swift swift.tar
fi

SITES_PASSED=`ls out/*out | wc -l`
HOME=$PWD
MAIL="$HOME/Mail.content"
#VERSION=`grep "^Swift.*swift-.*cog.*" remote_driver.stdout  | cut -d " " -f -2 | sed 's/\ /-/g' | tr '[A-Z]' '[a-z]'`
VERSION="swift-trunk"

cd out;
echo "Test-Results"                         >  $MAIL
echo "==========================================================================">> $MAIL
echo "Test home         : $PWD"             >> $MAIL
echo "Sites successful  : $SITES_PASSED"    >> $MAIL
#VERSION=`swift -version | cut -d " " -f -2 | sed 's/\ /-/g' | tr '[A-Z]' '[a-z]'`
for i in `ls *tar`
do
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++">> $MAIL
    #scp $i ci:~/public_html/$VERSION/
    tar -xf $i
    folder=`tar -tf $i | head -n 1 | sed 's/\/$//'`
    echo "Results for site : $folder"              >> $MAIL

    echo "GROUPS" | awk '{printf "%-40s\n", $0}' > t0
    grep "Group" $folder/tests*html | sed 's/Group\ /G/' | cut -c -40 | awk '{printf "%-40s\n", $0}'  >> t0

    echo "RUN "    | awk '{printf "%-5s\n", $0}'  >t1
    grep "Tests run" $folder/tests*html       | sed 's/Tests\ run//'  | awk '{printf "%-5s\n", $0}'    >> t1

    echo "PASS"   | awk '{printf "%-5s\n", $0}'  >t2
    grep "Tests succeeded" $folder/tests*html | sed 's/Tests\ succeeded.//' | awk '{printf "%-5s\n", $0}'  >> t2

    echo "FAIL"   | awk '{printf "%-5s\n", $0}'  >t3
    grep "Tests failed" $folder/tests*html    | sed 's/Tests\ failed.//' | awk '{printf "%-5s\n", $0}'  >> t3

    echo "TIME"   | awk '{printf "%-40s\n", $0}' >t4
    grep "Total Time" $folder/tests*html      | sed 's/Total\ Time://'  | awk '{printf "%-5s\n", $0}' >> t4

    pr -s'|' -tm t0 t1 t2 t3 t4 >> $MAIL
    rm t{0,1,2,3,4}
    HTML=$(basename $(ls $folder/tests*html))
    echo "Link: $PUBLISH_SERVER/$VERSION/$folder-$DATE/$HTML" >> $MAIL
    mkdir -p $PUBLISH_LOCATION/$VERSION
    mv $folder $PUBLISH_LOCATION/$VERSION/$folder-$DATE
done
cd $HOME;
cd ..;
RUN_DIR=$(basename $PWD);
cd ..;

folder=$RUN_DIR
HTML=$(basename $(ls $folder/tests*html))

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++">> $MAIL
echo "Results for site : LOCAL at $RUN_DIR-$DATE"              >> $MAIL
echo "GROUPS" | awk '{printf "%-40s\n", $0}' > t0
grep "Group" $folder/tests*html | sed 's/Group\ /G/' | cut -c -40 | awk '{printf "%-40s\n", $0}'  >> t0

echo "RUN "    | awk '{printf "%-5s\n", $0}'  >t1
grep "Tests run" $folder/tests*html       | sed 's/Tests\ run//'  | awk '{printf "%-5s\n", $0}'    >> t1

echo "PASS"   | awk '{printf "%-5s\n", $0}'  >t2
grep "Tests succeeded" $folder/tests*html | sed 's/Tests\ succeeded.//' | awk '{printf "%-5s\n", $0}'  >> t2

echo "FAIL"   | awk '{printf "%-5s\n", $0}'  >t3
grep "Tests failed" $folder/tests*html    | sed 's/Tests\ failed.//' | awk '{printf "%-5s\n", $0}'  >> t3

echo "TIME"   | awk '{printf "%-40s\n", $0}' >t4
grep "Total Time" $folder/tests*html      | sed 's/Total\ Time://'  | awk '{printf "%-5s\n", $0}' >> t4

pr -s'|' -tm t0 t1 t2 t3 t4 >> $MAIL
rm t{0,1,2,3,4}

echo "RUN_DIR = $RUN_DIR"
echo "Moving $RUN_DIR to $PUBLISH_LOCATION/$RUN_DIR-$DATE"
echo "Link: $PUBLISH_SERVER/$VERSION/$RUN_DIR-$DATE/$HTML"  >> $MAIL
echo "==========================================================================">> $MAIL
cat $MAIL

echo "Mail: mailx -s \"Test results from $HOSTNAME\" -r $FROM_MAIL ${TO_MAIL[*]}  < $MAIL"

mailx -s "Test results from $HOSTNAME" -r $FROM_MAIL ${TO_MAIL[*]}  < $MAIL

cp -R $folder $PUBLISH_LOCATION/$VERSION/$RUN_DIR-$DATE &

echo "Publishing links to CI from $MAIL"
grep -o "http.*" $MAIL | tee result_links.txt

DATE=$(date +%Y-%m-%d)
echo "Posting result_links.txt to /ci/www/projects/swift/tests/$VERSION/run-$DATE/"
cat result_links.txt
scp result_links.txt ci:/ci/www/projects/swift/tests/$VERSION/run-$DATE/

exit 0
