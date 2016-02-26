type file;

// Namd 
app (file out, file err) namd_wrapper (file psf_file, file pdb_file, 
                                       file coord_restart_file, file velocity_restart_file, 
                                       file system_restart_file, file namd_conf, file charmm_params)
{
   bgsh "/soft/applications/namd2-cvs20130304/namd2-pami"  @namd_conf stdout=@out stderr=@err; 
}

// Plotting script
/*app (file png) plot (file[] namdlogs)
{
    plot @png @namdlogs;
}
*/

// Range of nodes to test on
int min=1;
int max=100;
int delta=1;

// Files
file psf <"input_files/h0_solvion.psf">;
file pdb <"input_files/h0_solvion.pdb">;
file coord_restart <"input_files/h0_eq.0.restart.coor">;
file velocity_restart <"input_files/h0_eq.0.restart.vel">;
file system_restart <"input_files/h0_eq.0.restart.xsc">;
file namd_config <"input_files/h0_eq.conf">;
file charmm_parameters <"input_files/par_all22_prot.inp">;
file logs[];

// Run scaling tests
foreach i in [min:max:delta] {
   file namd_output <single_file_mapper; file=strcat("logs/h0scale.", i, ".out")>;
   file namd_error <single_file_mapper; file=strcat("logs/h0scale.", i, ".err")>;
   (namd_output, namd_error) = namd_wrapper(psf, pdb, coord_restart, velocity_restart, 
                                            system_restart, namd_config, charmm_parameters);
   logs[i] = namd_output;
}

// Plot output
/*
file plot_output <"scale.dat">;
plot_output = plot(logs);
*/
