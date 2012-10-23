
// SKIP-THIS-TEST

type file;

app (file o) copy (file i)
{
  cp @i @o;
}

// Will be transformed by CDM to 301/301-input.txt:
file f1<"301-input.txt">;

file f2<"301-output.txt">;

f2 = copy(f1);
