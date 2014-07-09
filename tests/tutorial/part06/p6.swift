type file;

# app() functions for application programs to be called:

app (file out) genseed (int nseeds, file seed_script)
{
  sh "simulate.sh" "-r" 2000000 "-n" nseeds stdout=@out;
}

app (file out) genbias (int bias_range, int nvalues, file bias_script)
{
  sh "simulate.sh" "-r" bias_range "-n" nvalues stdout=@out;
}

app (file out, file log) simulation (int timesteps, int sim_range,
                                     file bias_file, int scale, int sim_count, 
                                     file sim_script, file seed_file)
{
  sh "simulate.sh" "-t" timesteps "-r" sim_range "-B" @bias_file "-x" scale
           "-n" sim_count "-S" @seed_file stdout=@out stderr=@log;
}

app (file out, file log) analyze (file s[], file stat_script)
{
  sh "stats.sh" filenames(s) stdout=@out stderr=@log;
}

# Command line arguments

int  nsim  = toInt(arg("nsim",   "10"));  # number of simulation programs to run
int  steps = toInt(arg("steps",  "1"));   # number of timesteps (seconds) per simulation
int  range = toInt(arg("range",  "100")); # range of the generated random numbers
int  values = toInt(arg("values", "10"));  # number of values generated per simulation

# Main script and data

file simulate_script <"simulate.sh">;
file stats_script <"stats.sh">;
file seedfile <"output/seed.dat">;        # Dynamically generated bias for simulation ensemble

tracef("\n*** Script parameters: nsim=%i range=%i num values=%i\n\n", nsim, range, values);
seedfile = genseed(1,simulate_script);

file sims[];                      # Array of files to hold each simulation output

foreach i in [0:nsim-1] {
  file biasfile <single_file_mapper; file=strcat("output/bias_",i,".dat")>;
  file simout   <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  file simlog   <single_file_mapper; file=strcat("output/sim_",i,".log")>;
  biasfile = genbias(1000, 20, simulate_script);
  (simout,simlog) = simulation(steps, range, biasfile, 1000000, values, simulate_script, seedfile);
  sims[i] = simout;
}

file stats_out<"output/average.out">;
file stats_log<"output/average.log">;
(stats_out,stats_log) = analyze(sims, stats_script);
