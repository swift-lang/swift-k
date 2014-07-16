#
# Program to set up tutorial PATH
# Run as: source setup.sh
#

# Add tutorial apps to $PATH
TUTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export PATH=$TUTDIR/bin:$PATH
