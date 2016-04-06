type file;

// Rosetta
app (file _scorefile, file out, file err) rosetta (file pdb_file, int nstruct)
{
   bgsh "/home/ketan/openmp-gnu-july16-mini/build/src/debug/linux/2.6/64/ppc64/xlc/static-mpi/FlexPepDocking.staticmpi.linuxxlcdebug" "-database" "/home/ketan/minirosetta_database" "-pep_refine" "-s" @pdb_file "-ex1" "-ex2aro" "-use_input_sc" "-nstruct" nstruct "-overwrite" "-scorefile" @_scorefile stdout=@out stderr=@err; 
}

// Files
file pdb_files[] <filesys_mapper; location="hlac_complex", suffix=".pdb">;
file logs[];

// Run scaling tests
foreach pdb,i in pdb_files {
   file rosetta_output <single_file_mapper; file=strcat("logs/rosetta.", i, ".out")>;
   file rosetta_error <single_file_mapper; file=strcat("logs/rosetta.", i, ".err")>;
   file scorefile <single_file_mapper; file=strcat("score.",i, ".sc")>;
   (scorefile, rosetta_output, rosetta_error) = rosetta(pdb, 2);
   //logs[i] = rosetta_output;
}

