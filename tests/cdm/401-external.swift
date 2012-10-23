
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

file f1<"210-input.txt">;
file f2<"210-output.txt">;
f2 = copy(f1);
