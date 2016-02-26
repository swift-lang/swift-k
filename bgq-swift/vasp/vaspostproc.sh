#!/bin/bash

latt_factor=$4

echo Command line arguments: $@

cp OUTCAR output/vasp-outcar-${latt_factor}
cp CONTCAR output/vasp-contcar-${latt_factor}

