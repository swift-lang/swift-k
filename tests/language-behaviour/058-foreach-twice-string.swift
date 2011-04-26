type file;

app (file o) echo (string f)
{
  echo @f stdout=@o;
}

file out[]<simple_mapper; location=".", prefix="058-foreach-twice-string.first.",suffix=".out">;
file out2[]<simple_mapper; location=".", prefix="058-foreach-twice-string.second.",suffix=".out">;
string words[] = ["zero", "one", "two"];

foreach w,i in words {
  out[i] = echo(w);
}

foreach w,i in words {
  out2[i] = echo(w);
}

