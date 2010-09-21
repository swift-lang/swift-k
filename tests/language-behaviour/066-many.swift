
type file;

app p(file f)
{
  touch @f;
}

foreach i in [1:3000] {
  file f<"nop">;
  p(f);
}
