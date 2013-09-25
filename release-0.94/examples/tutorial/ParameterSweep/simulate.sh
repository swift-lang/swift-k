#! /bin/sh

#
# simulate.sh - a tiny script to model a "simulation application"
#               Reads N files of 2-column lines of the form "parameter value"
#               Computes a random function of (some of) the inputs

awk ' # Prints a single text line result with a random function of (some of) the input parameters

{ param[$1] = $2 } # read in the parameter values (for this member plus common files)

END {
  srand(param["n"] * param["rate"]) / param["beta"]; # the "simulation" :)
  printf ("Simulation number: %d alpha: %f beta: %f: result: %f\n", param["n"],
           param["alpha"], param["beta"], rand());
}

' $*  # member-file common-files...
