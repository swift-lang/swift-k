
type file;

app (file o) cps(file i, int s)
{
  cps @o @i s;
}

file input1<"201-input-1.txt">;
file input2<"201-input-2.txt">;

foreach i in [1:20]
{
  string s = @strcat("201-output-1-", i, ".txt");
  file output<single_file_mapper;file=s>;
  output = cps(input1, i);
}

foreach j in [1:20]
{
  string s = @strcat("201-output-2-", j, ".txt");
  file output<single_file_mapper;file=s>;
  output = cps(input2, 10+j);
}
