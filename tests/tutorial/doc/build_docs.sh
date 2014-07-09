#!/bin/bash -e

asciidoc -a icons -a toc -a toplevels=2 -a stylesheet=$PWD/asciidoc.css -a max-width=800px -o tutorial.html README

