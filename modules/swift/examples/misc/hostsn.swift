type file;

app (file o) hostname ()
{
  hostname stdout=@o;
}

file out[]<simple_mapper; location="outdir", prefix="f.",suffix=".out">;
foreach j in [1:@toInt(@arg("n","1"))] {
  out[j] = hostname();
}
