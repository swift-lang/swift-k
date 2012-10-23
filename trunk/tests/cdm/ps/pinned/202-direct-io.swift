
type file;

app (file o) merge (file i, file j)
{
  merge @i @j @o;
}

// Will be transformed by CDM to 202/input-[12].txt
file f1<"202-input-1.txt">;
file f2<"202-input-2.txt">;

// Will be transformed by CDM to 202/output.txt:
file f3<"202-output.txt">;

f3 = merge(f1, f2);
