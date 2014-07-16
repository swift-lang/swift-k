type file;

app (file o) cat (file i)
{
  cat @i stdout=@o;
}

file input_files[]<filesys_mapper; location="data", pattern="tmp.*">;

foreach j in input_files {
  file output<single_file_mapper; location="data", file=@strcat(@filename(j), ".out")>;
  output = cat(j);
}
