
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

file f1<"201-input.txt">;

// Will be transformed by CDM to 201/output.txt:
file f2<"201-output.txt">;

f2 = copy(f1);
