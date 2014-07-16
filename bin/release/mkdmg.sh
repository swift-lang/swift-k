#! /bin/sh
# 
# usage mkdmg <folder> <imagename>
#
#     will create <imagename>.dmg
#
# changes:
#    - added the imagename as parameter
#    - changed ditto to cp to avoid su and keep permissions
#
FOLDER="$1"
IMAGENAME="$2"
if [ -z "$FOLDER" ]; then
  echo "\nusage: $0 <folder> <imagename>\n"
  exit 1
fi

if [ ! -d "$FOLDER" ]; then
  echo $FOLDER does not exist
  exit 2
fi

SIZE=`du -s "$FOLDER" | awk '{ print $1 }'`
# allow space for partition map and directory structure
SIZE=`echo 1024 + $SIZE \* 1.1 / 1 | bc`
#NAME=`basename "$FOLDER"`
NAME=$IMAGENAME
FILE=$NAME.dmg
TMP=${TMP:-/tmp}

if [ $SIZE -lt 9216 ]; then
  SIZE=9216
fi

if [ -e "$FILE" ] ; then
  echo $FILE already exists!
  exit 3
fi

TMPFILE=$TMP/$$.dmg

echo Creating $TMPFILE from $FOLDER, $SIZE sectors...
hdiutil create $TMPFILE -sectors $SIZE -ov
if [ $? -ne 0 ] ; then
  rm -f $TMPFILE
  exit 4
fi
echo

DEVICES=`hdid -nomount $TMPFILE`
DEVMASTER=`echo $DEVICES| awk '{ print $1 }'`
DEVHFS=`echo $DEVICES| awk '{ print $5 }'`
echo Creating HFS partition $NAME on $TMPFILE at $DEVHFS
newfs_hfs -v "$NAME" $DEVHFS
if [ $? -ne 0 ] ; then
   rm -f $TMPFILE
exit 5
fi
hdiutil eject $DEVMASTER
if [ $? -ne 0 ] ; then
  rm -f $TMPFILE
  exit 6
fi
DEVICES=`hdid $TMPFILE`
  if [ $? -ne 0 ] ; then
  rm -f $TMPFILE
  exit 7
fi

DEVMASTER=`echo $DEVICES| awk '{ print $1 }'`
DEVHFS=`echo $DEVICES| awk '{ print $5 }'`
echo Copying $FOLDER to /Volumes/$NAME on $DEVMASTER
cp -r "$FOLDER" "/Volumes/$NAME"
# sudo ditto -rsrcFork "$FOLDER" "/Volumes/$NAME"
if [ $? -ne 0 ]; then
  hdiutil eject $DEVMASTER
  rm -f $TMPFILE
  exit 8
fi

hdiutil eject $DEVMASTER
if [ $? -ne 0 ]; then
  #rm -f $TMPFILE
  exit 9
fi

echo "Compressing $NAME to $FILE"
#hdiutil convert $TMPFILE -format UDZO -o "$FILE"
hdiutil convert $TMPFILE -format UDZO -imagekey zlib-level=9 -o "$FILE"
if [ $? -ne 0 ]; then
  rm -f "$FILE" $TMPFILE
  exit 10
fi

rm -f $TMPFILE

# end
