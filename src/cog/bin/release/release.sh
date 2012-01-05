#!/bin/bash


providers() {
	DISTNAME=$1

	for provider in `cat providers`; do
		make $DISTNAME-provider-$provider-bin
	done
}

cog-version() {
CVSROOT=$1
if [ "$CVSROOT" == "" ]; then
	CVSROOT="https://svn.sourceforge.net/svnroot/cogkit"
fi
TAG=$2
if [ "$TAG" == "" ]; then
	TAG=now
fi

	if [ "$LOG" == "" ]; then
		LOG="log.txt"
	fi

	if [ ! -f src/cog/VERSION ]; then
		mkdir -p src/cog
		if [ "$TAG" == "now" ]; then
			#cvs -d $CVSROOT export -D now src/cog/VERSION 2>>$LOG
			svn export --force $CVSROOT/trunk/current/src/cog/VERSION src/cog/VERSION 2>>$LOG
		else
			#cvs -d $CVSROOT export $TAG src/cog/VERSION 2>>$LOG
			svn export --force $CVSROOT/tags/$TAG/src/cog/VERSION src/cog/VERSION 2>>$LOG
		fi
	fi


	VERSION=`gawk -F: 'function trim(s){gsub(/^[ \t\n]*/,"",s); gsub(/[ \t\n]*$/,"",s);return s} BEGIN {FS="="} {print trim($2)}' src/cog/VERSION`

	echo $VERSION
	echo $VERSION >>$LOG
}

checkout() {
CVSMODULE=$1
TAG=$2

	if [ "$TAG" == "now" ]; then
		echo "Checking out current $CVSMODULE from $CVSROOT"
		#cvs -q -d $CVSROOT export -D now $CVSMODULE >>$LOG
		svn export --force $CVSROOT/trunk/current/$CVSMODULE src/cog >>$LOG
	else
		echo "Checking out $TAG tag of $CVSMODULE"
		#cvs -q -d $CVSROOT export $TAG $CVSMODULE >>$LOG
		svn export --force $CVSROOT/tags/$TAG/$CVSMODULE src/cog >>$LOG
	fi
}

providerversion() {
PROVIDER=$1
CVSROOT=$2
TAG=$3

	if [ "$LOG" == "" ]; then
		LOG="log.txt"
	fi

	if [ ! -f src/cog/modules/provider-$PROVIDER/project.properties ]; then
		if [ "$TAG" == "now" ]; then
			#cvs -d $CVSROOT export -D now src/cog/modules/provider-$PROVIDER/project.properties 2>>$LOG
			svn export --force $CVSROOT/trunk/current/src/cog/modules/provider-$PROVIDER/project.properties src/cog/modules/provider-$PROVIDER 2>>$LOG
		else
			#cvs -d $CVSROOT export $TAG src/cog/modules/provider-$PROVIDER/project.properties 2>>$LOG
			svn export --force $CVSROOT/tags/$TAG/src/cog/modules/provider-$PROVIDER/project.properties src/cog/modules/provider-$PROVIDER 2>>$LOG
		fi
	fi


	MODULEVERSION=`grep "version" src/cog/modules/provider-$PROVIDER/project.properties | gawk -F: 'function trim(s){gsub(/^[ \t\n]*/,"",s); gsub(/[ \t\n]*$/,"",s);return s} BEGIN {FS="="} {print trim($2)}'`
	echo $MODULEVERSION
}

publish() {
PUBLISHHOST=$1
PUBLISHDIR=$2
	
	echo "About to publish files to $PUBLISHDIR on $PUBLISHHOST"
	echo "Last chance to bail out"
	read -e -p "Are you sure you want to continue (y/n): "
	if [ "$REPLY" != "y" ]; then
		exit 1
	fi
	case $PUBLISHHOST in
		localhost)
			#copy
			mkdir -p $PUBLISHDIR
			cp -r out/* $PUBLISHDIR
			;;
		*)
			#securecopy
                        ssh $PUBLISHHOST mkdir -p $PUBLISHDIR
                        scp -r out/* $PUBLISHHOST:$PUBLISHDIR
			;;			
	esac
}

lock() {
PUBLISHHOST=$1
PUBLISHDIR=$2
	
	echo "About to make all files in $PUBLISHDIR on $PUBLISHHOST read-only"
	echo "Last chance to bail out"
	read -e -p "Are you sure you want to continue (y/n): "
	if [ "$REPLY" != "y" ]; then
		exit 1
	fi
	case $PUBLISHHOST in
		localhost)
			chmod -R -w $PUBLISHDIR
			;;
		*)
			ssh $PUBLISHHOST chmod -R -w $PUBLISHDIR
			;;			
	esac
}

OP=$1

if [ "$CVSROOT" == "" ]; then
	CVSROOT="https://svn.sourceforge.net/svnroot/cogkit"
fi

case $OP in
	checkout)
		checkout $2 $3
		;;
	cogversion)
		cog-version $2 $3
		;;
	providerversion)
		providerversion $2 $3 $4
		;;
	providers)
		providers $2
		;;
	publish)
		publish $2 $3
		;;
	lock)
		lock $2 $3
		;;
	*)
		echo "Invalid option: $OP" >&2
		exit 1
		;;
esac
