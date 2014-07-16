
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

file f1<"205-input.txt">;

// Will be transformed by CDM to /tmp/211-output.txt (no staging)
file f2<"/tmp/205-output.txt">;

f2 = copy(f1);
