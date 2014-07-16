
type file;

app (file o) copy (file i)
{
  cp @i @o;
}

// Will be transformed by CDM to /tmp/206-input.txt (no staging)
file f1<"/tmp/206-input.txt">;

file f2<"206-output.txt">;

f2 = copy(f1);
