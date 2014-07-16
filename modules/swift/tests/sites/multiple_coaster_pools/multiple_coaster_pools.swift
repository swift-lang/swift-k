type file;

app (file o) sleep_single (file s, int delay)
{
  bash_single @s delay stdout=@o;
}

app (file o) sleep_multiple (file s, int delay)
{
  bash_multiple @s delay stdout=@o;
}

file sleepscript <"hostsnsleep.sh">;

foreach i in [1:@toInt(@arg("n","50"))] {
  file out_single <single_file_mapper; file=@strcat("output/output.single.", i, ".log")>;
  out_single = sleep_single(sleepscript, 1);
  
  file out_multiple <single_file_mapper; file=@strcat("output/output.multiple.", i, ".log")>;
  out_multiple = sleep_multiple(sleepscript, 16);
}
