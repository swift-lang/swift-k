#! /bin/sh

# scp -r swift-cray-tutorial.html images *png login.ci.uchicago.edu:/ci/www/projects/swift/tutorials/cray

tar zcf - --exclude-vcs *html *png images | ssh login.ci.uchicago.edu "cd /ci/www/projects/swift/tutorials/cray; tar zxf -"
