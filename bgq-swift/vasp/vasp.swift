type file;

app (file _o, file _e, file _outcar, file _contcar) runvasp (file _vaspreproc, file _vaspostproc, file _vasp_incar, file _vasp_poscar, file _vasp_potcar, file _vasp_kpoints, int _latt_factor) {
  #bgsh @_vasprun @_vasp_incar @_vasp_poscar @_vasp_potcar @_vasp_kpoints _latt_factor stdout=@_o stderr=@_e;
  bgsh "/home/naromero/vasp.5.3.3.bgq.power_elpa_sep2013/vasp.bgq.ibm.real" @_vaspreproc @_vaspostproc _latt_factor stdout=@_o stderr=@_e;
}

# Files
file vaspreproc <"vaspreproc.sh">;
file vaspostproc <"vaspostproc.sh">;

file incar <"INCAR">;
file poscar <"POSCAR">;
file potcar <"POTCAR">;
file kpoints <"KPOINTS">;

foreach fact in [1:300]{
  file output <single_file_mapper; file=strcat("logs/vasp-", fact, ".out.txt")>;
  file error  <single_file_mapper; file=strcat("logs/vasp-", fact, ".err.txt")>;
  file outcar <single_file_mapper; file=strcat("output/vasp-outcar-", fact)>;
  file contcar <single_file_mapper; file=strcat("output/vasp-contcar-", fact)>;
  (output, error, outcar, contcar) = runvasp(vaspreproc, vaspostproc, incar, poscar, potcar, kpoints, fact);
}
