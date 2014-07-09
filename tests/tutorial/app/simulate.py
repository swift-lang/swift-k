#!/usr/bin/env python
import sys
import os
import socket
import time
import math
import random
import getpass

def parse():
    from optparse import OptionParser
    parser=OptionParser()
    parser.add_option("-b", "--bias", type="int", dest="bias", default=0,
                      help="offset bias: add this integer to all results");

    parser.add_option("-B", "--biasfile", type="string", dest="biasfile",
                      default="none", help="file of integer biases to add to results" );

    parser.add_option("-l", "--log", type="string", dest="log", default="yes",
                      help="generate a log in stderr if not nul");

    parser.add_option("-n", "--nvalues", type="int", dest="nvalues",default=1,
                      help="print this many values per simulation" );

    parser.add_option("-s", "--seed", type="int", dest="initseed", default=0,
                      help="use this integer [0..32767] as a seed");

    parser.add_option("-S", "--seedfile", type="string", dest="seedfile",
                      default="none", help="use this file (containing integer seeds [0..32767]) one per line" );

    parser.add_option("-t", "--timesteps", type="int", dest="timesteps",
                      default=0, help='number of simulated "timesteps" in seconds (determines runtime)' );

    parser.add_option("-r", "--range", type="int", dest="range", default=100,
                      help="range (limit) of generated results" );

    parser.add_option("-w", "--width", type="int", dest="width", default=8,
                      help="Width ?" );

    parser.add_option("-x", "--scale", type="int", dest="scale", default=1,
                      help="scale the results by this integer" );
    # Not implemented yet
    parser.add_option("-p", "--paramfile", type="string", dest="paramfile", default="none",
                      help="Not implemented yet" );
    return (parser.parse_args());

def log():
    import datetime
    print >> sys.stderr, "Called as: ", str(sys.argv)
    print >> sys.stderr, "Start time: ", datetime.datetime.now()
    print >> sys.stderr, "Running on node: ", socket.gethostname()
    print >> sys.stderr, "Running as user: ", getpass.getuser()
    print >> sys.stderr, "\nEnvironment:\n\n"
    print >> sys.stderr, os.environ

def printparams(options):
    print 'bias =',options.bias
    print 'biasfile = ',options.biasfile
    print 'log = ', options.log
    print 'nvalues = ',options.nvalues
    print 'seed = ', options.initseed
    print 'seedfile = ', options.seedfile
    print 'timesteps = ', options.timesteps
    print 'range = ', options.range
    print 'width = ', options.width
    print 'scale = ', options.scale
    print 'paramfile = ', options.paramfile

def simulate(options):
    time.sleep(options.timesteps)
    bias=[];
    if (options.biasfile != "none"):
        try:
            with open(options.biasfile) as biasfile:
                lines = biasfile.read().splitlines()
                for line in lines:
                    bias.append(int(line))
        except IOError:
            print "Error accessing content from file: ", options.biasfile 
    bias_count = len(bias)

    for i in range(options.nvalues):
        value = (random.random() +
        random.random()*math.pow(2,16) +
        random.random()*math.pow(2,32) +
        random.random()*math.pow(2,48))
        value=( (int(value)%options.range) * options.scale + options.bias)
        if ( i < bias_count ):
            value = value + bias[i]
        elif ( bias_count > 0 ):
            value = value + bias[bias_count-1]

        print '{num:{fill}{width}}'.format(num=value, fill=' ', width=options.width)


def seed(options):
    if (options.initseed != 0 ):
        random.seed(options.initseed)
    if (options.seedfile != "none"):
        try:
            with open(options.seedfile) as seedfile:
                lines=seedfile.read().splitlines()
                seed = 0
                for line in lines:
                    seed = seed + int(line)
            random.seed(seed)
        except IOError:
            print "Could not open file: ", options.seedfile


if __name__ == "__main__":
    (options, args) = parse()
    printparams(options)
    seed(options)
    simulate(options)
    log()









