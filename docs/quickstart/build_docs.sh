#!/bin/bash -e

asciidoc -a icons -a toplevels=2 -a stylesheet=$PWD/../swift-tutorial/doc/asciidoc.css -a max-width=800px -o quickstart.html quickstart.asc

