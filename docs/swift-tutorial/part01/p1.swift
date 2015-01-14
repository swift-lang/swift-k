type file;

app (file o) simulation ()
{
  simulate stdout=filename(o);
}

file f <"sim.out">;
f = simulation();
