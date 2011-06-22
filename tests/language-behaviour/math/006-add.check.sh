#!/bin/bash
set -x
(cat 006-add.out | grep 135) || exit 1
exit 0
#!/bin/bash
set -x
cat 006-add.out | grep || exit 1
exit 0
