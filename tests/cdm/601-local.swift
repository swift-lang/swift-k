
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

// Will be transformed by CDM to 601/601-input.txt:
file f1<"601-input.txt">;

file f2<"601-output.txt">;

f2 = copy(f1);
