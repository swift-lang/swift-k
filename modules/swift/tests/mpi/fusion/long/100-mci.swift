
type file;

app (file o) transform(file i)
{
  mpi_sleep "-i" @i "-o" @o 3;
}

file input<"100-input.txt">;

int total = 1;

foreach i in [0:total-1] {
  file output<single_file_mapper;file=@strcat("transform-",i,".txt")>;
  output = transform(input);
}
