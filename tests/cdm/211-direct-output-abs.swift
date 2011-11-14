
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

file f1<"211-input.txt">;

// Will be transformed by CDM to /tmp/211-output.txt:
file f2<"211-output.txt">;

f2 = copy(f1);
