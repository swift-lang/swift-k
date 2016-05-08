#!/bin/sh

TARGET=$PWD/../../../../../dist/swift-svn/libexec/aimes-emanager-provider
cp aimes-swift-client.py $TARGET
ln -s aimes-swift-client.py $TARGET/aimes-swift-submit.py
ln -s aimes-swift-client.py $TARGET/aimes-swift-status.py
ln -s aimes-swift-client.py $TARGET/aimes-swift-cancel.py

