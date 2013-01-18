#!/bin/sh

run() {
	checkerror
	LAST="$@"
	echo "$@"
	"$@"
}

checkerror() {
	if [ "$?" != "0" ]; then
		echo "$LAST failed"
		exit q
	fi
}

#autoreconf --force --install -I config -I . -I m4
run libtoolize -i -f -c -q
run aclocal
run automake --gnu --add-missing 
run autoconf
checkerror
echo "Done"
