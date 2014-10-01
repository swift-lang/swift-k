type file;

file script<"randfail.sh">;

app (file ofile1, file ofile2) quicklyFailingApp(file script, int failchance,
int delay)
{
  sh @script failchance delay stdout=@ofile1 stderr=@ofile2;
}

app (file ofile) someApp3(file ifile, file jfile, file kfile)
{
  sh "-c" @strcat("cat ",@filename(ifile)) stdout=@ofile;
}

app (file ofile) someApp(file ifile)
{
  sh "-c" @strcat("cat ",@filename(ifile)) stdout=@ofile;
}

app sleep (int sec)
{
  sh "-c" @strcat("sleep ",sec);
}

int sufficientlyLargeNumber = 100;

file a[];
foreach i in [0:sufficientlyLargeNumber] {
  file f1<single_file_mapper; file=@strcat("failed1.",i,".out")>;
  file f2<single_file_mapper; file=@strcat("failed2.",i,".out")>;
  (f1,f2)  = quicklyFailingApp(script,50,0);
  a[i] = someApp(f2);
}
