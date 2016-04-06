#!/soft/interpreters/python-2.7.9/powerpc64-linux-gnu/bin/python

import subprocess
import os

subprocess.call(["runjob", "-p", "16", "--np", "16", "--verbose=INFO", "--block", os.environ["COBALT_PARTNAME"], ":", "/home/ketan/SwiftApps/subjobs/mpicatsnsleep/mpicatnap", "data.txt", "out.txt", "1"])
