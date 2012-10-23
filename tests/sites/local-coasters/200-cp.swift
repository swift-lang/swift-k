
type file;

app (file o) cp(file i)
{
  cp @i @o;
}

file input<"200-input.txt">;
file output<"200-output.txt"> = cp(input);
