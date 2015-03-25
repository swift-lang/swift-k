type file;

# app() functions for application programs to be called:

app (file out) genseed (file _simulate, int nseeds)
{
  bgsh @_simulate "-r" 2000000 "-n" nseeds stdout=@out;
}

app (file out) genbias (file _stats, int bias_range, int nvalues)
{
  bgsh @_stats "-r" bias_range "-n" nvalues stdout=@out;
}

app (file out, file log) simulation (file _simulate, int timesteps, int sim_range, file bias_file, 
                                     int scale, int sim_count, file seed_file)
{
  bgsh @_simulate "-t" timesteps "-r" sim_range "-B" @bias_file "-x" scale
           "-n" sim_count "-S" @seed_file stdout=@out stderr=@log;
}

app (file out, file log) analyze (file _stats, file s[])
{
  bgsh @_stats filenames(s) stdout=@out stderr=@log;
}

# Command line arguments

int   nsim = toInt(arg("nsim",   "10"));  # number of simulation programs to run
int  steps = toInt(arg("steps",  "1"));   # number of timesteps (seconds) per simulation
int  range = toInt(arg("range",  "100")); # range of the generated random numbers
int values = toInt(arg("values", "10"));  # number of values generated per simulation

#App executables
file simulate<"simulate">;
file stats<"stats">;

# Main script and data
file seedfile <"output/seed.dat">;        # Dynamically generated bias for simulation ensemble

tracef("\n*** Script parameters: nsim=%i range=%i num values=%i\n\n", nsim, range, values);
seedfile = genseed(simulate, 1);

file sims[];                      # Array of files to hold each simulation output

foreach i in [0:nsim-1] {
  file biasfile <single_file_mapper; file=strcat("output/bias_",i,".dat")>;
  file simout   <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  file simlog   <single_file_mapper; file=strcat("output/sim_",i,".log")>;
  biasfile = genbias(stats, 1000, 20);
  (simout,simlog) = simulation(simulate, steps, range, biasfile, 1000000, values, seedfile);
  sims[i] = simout;
}

file stats_out<"output/average.out">;
file stats_log<"output/average.log">;
(stats_out,stats_log) = analyze(stats, sims);
