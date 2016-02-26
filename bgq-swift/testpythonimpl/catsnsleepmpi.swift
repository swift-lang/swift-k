type file;

app (file o, file sout, file serr) cat (file i)
{
  bgpy "/home/ketan/SwiftApps/subjobs/mpicatsnsleep/mpicatnap" @i @o arg("s","1") stdout=@sout stderr=@serr;
}

file out[]<simple_mapper; location="outdir", prefix="f.",suffix=".out">;

foreach i in [1:toInt(arg("n","1"))] {
  file data<"data.txt">;
  file sout<single_file_mapper; file=strcat("outdir/f.",i,".stdout")>;
  file serr<single_file_mapper; file=strcat("outdir/f.",i,".stderr")>;
  (out[i],sout,serr) = cat(data);
}
