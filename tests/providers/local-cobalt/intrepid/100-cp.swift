
type file;

app (file o) cp(file i)
{
  cp @i @o;
}

file input<"100-cp-input.txt">;
file output<"100-cp-output.txt">;

output = cp(input);

