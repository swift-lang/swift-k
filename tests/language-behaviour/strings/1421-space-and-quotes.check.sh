#!/bin/bash
set -x
[ -f 1421-space-and-quotes.2\"\ space\ .out ] || exit 1
[ -f 1421-space-and-quotes.3\'\ space\ .out ] || exit 1
[ -f 1421-space-and-quotes.2\ sp\"ace\ .out ] || exit 1
[ -f 1421-space-and-quotes.3\ sp\'ace\ .out ] || exit 1
[ -f 1421-space-and-quotes.2\'\ sp\"ac\"e\ .out ] || exit 1
[ -f 1421-space-and-quotes.3\'\ sp\'ac\'e\ .out ] || exit 1
[ -f 1421-space-and-quotes.\'\'\'\ \'\ \'\'\'\ \'\'\ \'\'\'\'\'\'\ \'\ \"\"\"\"\"\"\"\"\"\ \'\ \ \ \ \'\ \ \'\ \"\"\"\'\ \"\'\".out ] || exit 1
exit 0
