type file;

app (file o) cat (file i)
{
  cat @i stdout=@o;
}

file out[]<simple_mapper; location=".", prefix="003-catsn.",suffix=".out">;
foreach j in [1:@toint(@arg("n","10"))] {
  file data<"data.txt">;
  out[j] = cat(data);
}
