#!/bin/bash

incar=$1
poscar=$2
potcar=$3
kpoints=$4
latt_factor=$5

echo Command line arguments: $@
echo

sed -i "2s/.*/$latt_factor/" $poscar

/home/naromero/vasp.5.3.3.bgq.power_elpa_sep2013/vasp.bgq.ibm

cp OUTCAR output/vasp-outcar-${latt_factor}
cp CONTCAR output/vasp-contcar-${latt_factor}

