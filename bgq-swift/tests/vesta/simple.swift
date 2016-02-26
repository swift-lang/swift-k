type file;

app (file o, file sout, file serr) cat (file i)
{
  bgsh "/home/ketan/SwiftApps/subjobs/mpicatnap" @i @o arg("s","1") stdout=@sout stderr=@serr;
}

file out[]<simple_mapper; location="outdir", prefix="f.",suffix=".out">;

foreach j in [1:toInt(arg("n","1"))] {
  file data<"data.txt">;
  file sout<single_file_mapper; file=strcat("outdir/f.",j,".stdout")>;
  file serr<single_file_mapper; file=strcat("outdir/f.",j,".stderr")>;
  (out[j],sout,serr) = cat(data);
}

