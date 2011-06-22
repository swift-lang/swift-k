#!/bin/bash

set -x

rm -v 071-singlefilemapper-input.in \
      071-singlefilemapper-input.out || exit 1

exit 0
#!/bin/bash
set -x
rm -r 071-singlefilemapper-input-* 071-singlefilemapper-input.*ml 071-singlefilemapper-input.out || exit 1
exit 0
