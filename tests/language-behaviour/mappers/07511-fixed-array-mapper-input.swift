type messagefile;

/*
app p(messagefile t) {
  echo "foo" stdin=@filename(t);
}
*/
string fns="07511-fixed-array-mapper-input.first.in 07511-fixed-array-mapper-input.second.in 07511-fixed-array-mapper-input.third.in";

messagefile infile[] <fixed_array_mapper; files=fns>;

/*
p(infile[0]);
p(infile[1]);
p(infile[2]);
*/
tracef("file: %s\n", @filename(infile[0]));
tracef("file: %s\n", @filename(infile[1]));
tracef("file: %s\n", @filename(infile[2]));
