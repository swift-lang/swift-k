type file;

app (file out, file log) simulation (file _simulate, int sim_steps, int sim_range, int sim_values)
{
  bgsh @_simulate "--timesteps" sim_steps "--range" sim_range "--nvalues" sim_values stdout=@out stderr=@log;
}

app (file out, file log) analyze (file _stats, file s[])
{
  bgsh @_stats filenames(s) stdout=@out stderr=@log;
}

int nsim   = toInt(arg("nsim",   "10"));
int steps  = toInt(arg("steps",  "1"));
int range  = toInt(arg("range",  "100"));
int values = toInt(arg("values", "5"));

file simulate<"simulate">;
file stats<"stats">;

file sims[];

foreach i in [0:nsim-1] {
  file simout <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  file simlog <single_file_mapper; file=strcat("output/sim_",i,".log")>;
  (simout,simlog) = simulation(simulate, steps,range,values);
  sims[i] = simout;
}

file stats_out<"output/average.out">;
file stats_log<"output/average.log">;
(stats_out, stats_log) = analyze(stats, sims);
