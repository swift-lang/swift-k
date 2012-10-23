type file;

# Application to generate the parameter and data files

app (file m[], file c[]) genSweep (int nm, int nc)  
{
  gensweep nm @filename(m[0]) nc @filename(c[0]);
}

# Application to perform a "simulation"

app (file o) simulation (file f, file common[])
{
  simulate stdout=@filename(o) @filename(f) @filenames(common);
}

# Set the size of the parameter sweep

int nMembers = @toint(@arg("nMembers","5"));   // number of members in the simulation
int nCommon  = @toint(@arg("nCommon","3"));    // number of common files to each sim
tracef("Running parameter sweep ensemble of %i members with %i common files\n", nMembers, nCommon);

# Generate the file names to use

string mName[];
string oName[];
string cName[];

foreach i in [0:nMembers-1] {
  mName[i] = @sprintf("member.%i",i);
  oName[i] = @sprintf("result.%i",i);
}

foreach i in [0:nCommon-1] {
  cName[i] = @sprintf("common.%i",i);
}

# Set the file names to use

file mFile[] <array_mapper; files=mName>;
file cFile[] <array_mapper; files=cName>;
file oFile[] <array_mapper; files=oName>;

# Generate the files for the ensemble run

(mFile, cFile) = genSweep(nMembers, nCommon);

# Perform the ensemble of parallel simulations

foreach f, i in mFile {
  oFile[i] = simulation(f, cFile);
}



/* For debugging:

trace("mFiles",@filenames(mFile));
trace("oFiles",@filenames(oFile));
trace("cFiles",@filenames(cFile));

*/