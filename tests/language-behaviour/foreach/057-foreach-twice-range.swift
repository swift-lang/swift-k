type file;

app (file o) echo (string msg)
{
  echo msg stdout=@o;
}

file out[]<simple_mapper; location=".", prefix="057-foreach-twice-range.first.",suffix=".out">;
file out2[]<simple_mapper; location=".", prefix="057-foreach-twice-range.second.",suffix=".out">;

foreach a,i in [0:9] {
  string s = @strcat("test1-", i);
  out[i] = echo(s);
}

foreach a,i in [0:9] {
  string s = @strcat("test2-", i);
  out2[i] = echo(s);
}

