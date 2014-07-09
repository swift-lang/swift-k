type file;

app (file out, file log) simulation (int sim_steps, int sim_range, int sim_values, file sim_script)
{
  sh "simulate.sh" "--timesteps" sim_steps "--range" sim_range "--nvalues" sim_values stdout=@out stderr=@log;
}

app (file out, file log) analyze (file s[], file stat_script)
{
  sh "stats.sh" filenames(s) stdout=@out stderr=@log;
}

int nsim   = toInt(arg("nsim",   "10"));
int steps  = toInt(arg("steps",  "1"));
int range  = toInt(arg("range",  "100"));
int values = toInt(arg("values", "5"));

file sims[];
file simulate_script <"simulate.sh">;
file stats_script <"stats.sh">;

foreach i in [0:nsim-1] {
  file simout <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  file simlog <single_file_mapper; file=strcat("output/sim_",i,".log")>;
  (simout,simlog) = simulation(steps,range,values,simulate_script);
  sims[i] = simout;
}

file stats_out<"output/average.out">;
file stats_log<"output/average.log">;
(stats_out, stats_log) = analyze(sims,stats_script);
