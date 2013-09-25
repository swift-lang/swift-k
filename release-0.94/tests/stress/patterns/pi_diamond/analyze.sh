#!/bin/bash

awk '
$3 < 1.0 { inCircle++; }
         { n++ }
END {
  print inCircle * 4.0 / n
}' $*
