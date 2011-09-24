type file;

app (file o) cat (file i)
{
  cat @i stdout=@o;
}

file out[]<simple_mapper; location=".", prefix="catsn.",suffix=".out">;
foreach j in [1:@toint(@arg("n","1"))] {
  file data<"data.txt">;
  out[j] = cat(data);
}
