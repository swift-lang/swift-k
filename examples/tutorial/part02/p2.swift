type file;

app (file o) simulation ()
{
  simulate stdout=filename(o);
}

foreach i in [0:9] {
  file f <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  f = simulation();
}
