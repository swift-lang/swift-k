
type file;

app (file o) copy(file i)
{
  mpi_cp @i @o;
}

file input<"100-input.txt">;
file output<"100-output.txt">;

output = copy(input);
