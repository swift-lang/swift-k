#!/bin/bash

data="fjkdlsfjlkdsjflka"
mkdir -p data/foo_a
mkdir -p data/foo_b
echo $data >> data/foo_a/foo_a.txt
echo $data >> data/foo_a/foo_b.txt
echo $data >> data/foo_b/bar_1
echo $data >> data/foo_b/bar_2
echo $data >> data/data.txt
