type file;

app (file o) cat (file i)
{
  cat @i stdout=@o;
}

string t = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
string char[] = @strsplit(t, "");  

file out[]<simple_mapper; location=".", prefix="catsn.",suffix=".out">;
foreach j in [1:@toInt(@arg("n","10"))] {
  file data<"data.txt">;
  out[j] = cat(data);
}
