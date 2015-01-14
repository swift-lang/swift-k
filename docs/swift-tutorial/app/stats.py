#!/usr/bin/env python
import socket
import sys
import os

def log():
    import datetime
    print >> sys.stderr, "Called as: ", str(sys.argv)
    print >> sys.stderr, "Start time: ", datetime.datetime.now()
    print >> sys.stderr, "Running on node: ", socket.gethostname()
    print >> sys.stderr, "Running as user: ",os.getlogin()
    print >> sys.stderr, "\nEnvironment:\n\n"
    print >> sys.stderr, os.environ

def stat():
    total = 0
    count = 0
    for f in sys.argv[1:]:
        try:
            with open(f) as ifile:
                lines = ifile.read().splitlines()
                for line in lines:
                    total += int(line)
                    count += 1
        except IOError:
            print "Error accessing content from file: ", options.biasfile
        print total/count

if __name__ == "__main__":
    log()
    stat()
