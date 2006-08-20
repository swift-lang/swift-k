USER=`whoami`
VERSION=`head -1 VERSION.txt`
DESTINATION=${USER}@cvs.cogkit.org:/www/www.cogkit.org
RELEASEDIR=release
QCDIR=${RELEASEDIR}/${VERSION}/qc


publish:
	echo Make sure 
	echo ${USER}@cvs.cogkit.org:/www/www.cogkit.org/releases/${VERSION} exits
	scp qualitycontrol/findbugs.html ${USER}@cvs.cogkit.org:/www/www.cogkit.org/release/${VERSION}

install-qc:
	cd ../..; cvs -d ${USER}@cvs.cogkit.org:/cvs/cogkit co src/tools

findbugs:
	echo "You can install findbugs by calling 'make install-qc'"
	cd qualitycontrol; '../../tools/findbugs-0.8.6/bin/findbugs.bat' -textui -low -html -project findbugs.fb > findbugs.html

publish-findbugs:
	echo "publish the result of findbugs to the web"
	cp findbugs.html ???


getpmd:
	cd ../..; cvs -d ${USER}@cvs.cogkit.org:/cvs/cogkit co src/pmd
