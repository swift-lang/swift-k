
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

// Will be transformed by CDM to 602/602-input.txt:
file f1<"602-input.txt">;

file f2<"602-output.txt">;

f2 = copy(f1);
